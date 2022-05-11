package fr.insee.metallica.pocprotools.command.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.command.domain.Command.Status;
import fr.insee.metallica.pocprotools.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.pocprotools.command.exception.CommandExecutionRetryException;
import fr.insee.metallica.pocprotools.command.processor.CommandProcessor;
import fr.insee.metallica.pocprotools.command.repository.CommandRepository;
import fr.insee.metallica.pocprotools.command.service.CommandEventListener.Type;

@Service
public class CommandEngine {
	static final Logger log = LoggerFactory.getLogger(CommandEngine.class);
	
	@Value("${command.schedule.timeWithoutHeartBeatBeforeDeath:15}")
	private int timeWithoutHeartBeatBeforeDeath;
	
	@Value("${command.schedule.delayHeartBeat:5}")
	private int delayHeartBeat;

	@Autowired
	private CommandService commandService;

	@Autowired
	private CommandRepository commandRepository;
	
	private final Set<UUID> currentCommands = new HashSet<UUID>();
	
	private final Map<String, CommandProcessor> processors = new HashMap<>();
	
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	@Scheduled(fixedDelayString = "${command.schedule.delayHeartBeat:5}", timeUnit = TimeUnit.SECONDS)
	public void updateOwnedCommand() {
		List<UUID> currentCommands;
		synchronized (this.currentCommands) {
			currentCommands = List.copyOf(this.currentCommands);
		}
		for (var id : currentCommands) {
			commandService.heartBeat(id);
		}
	}
	
	@Scheduled(fixedDelayString = "${command.schedule.delayBeetweenRetryCheck:5}", timeUnit = TimeUnit.SECONDS)
	public void checkForScheduledCommand() {
		for (var command : commandRepository.findCommandsScheduled(100)) {
			process(command.getId());
		}
	}
	
	@Scheduled(fixedDelayString = "${command.schedule.delayBeetweenDeadCheck:5}", timeUnit = TimeUnit.SECONDS)
	public void checkForDeadCommand() {
		for(int i = 0; i < 100; i++) {
			var page = commandRepository.findPageByStatusAndLastHeartBeatLessThanEqual(Status.Processing, LocalDateTime.now().minusSeconds(timeWithoutHeartBeatBeforeDeath), PageRequest.of(0, 10));
			if (page.getNumberOfElements() == 0) return;
			
			page.stream().forEach((c) -> { 
				commandRepository.setStatus(c.getId(), Status.Processing, Status.Pending);
				process(c.getId());
			});
		}
	}
	
	@PostConstruct
	public void registerCommandEvent() {
		commandService.subscribe((command, data) -> registerAfterCommit(() -> this.process(command.getId())), Type.Added);
	}
	
	public void process(UUID commandId) {
		executorService.execute(() -> processThread(commandId));
	}
	
	private void processThread(UUID commandId) {
		var command = commandService.aquireToProcess(commandId);
		if(command != null) {
			try {
				synchronized (currentCommands) {
					currentCommands.add(command.getId());
				}
				if (limitReached(command)) {
					rescheduleIfProcessing(commandId);
					return;
				}
				var processor = processors.get(command.getType());
				if (processor == null) throw new CommandExecutionAbortException("Processor for " + command.getType() + " not found");
				
				log.info("Starting command {}", command.getId());
				commandService.publish(Type.Processing, command);
				
				var result = processor.process(command);
				commandService.done(command, result);
				
				log.info("Command executed {}", command.getId());
			} catch (CommandExecutionAbortException e) {
				log.error("Error in command " + command.getId() + " abort", e);
				commandService.error(command, e.getMessage());
			} catch (CommandExecutionRetryException e) {
				log.error("Error in command " + command.getId() + " retry", e);
				commandService.retry(command, e.getMessage(), e.getDelayInSeconds());
			} catch (Exception e) {
				log.error("Unmanaged error in command " + command.getId() + " abort", e);
				commandService.error(command, e.getMessage());
			} finally {
				synchronized (currentCommands) {
					currentCommands.remove(command.getId());
				}
			}
		}
	}
	
	private boolean limitReached(Command command) {
		if (command.getConcurrencyLimit() <= 0) return false;
		
		return commandRepository.countByLimitKeyAndStatus(command.getLimitKey(), Status.Processing) > command.getConcurrencyLimit();
	}

	@PreDestroy
	@Transactional
	public void destroy() throws InterruptedException {
		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.SECONDS);
		
		for (var c : currentCommands) {
			rescheduleIfProcessing(c);
		}
	}

	private void rescheduleIfProcessing(UUID commandId) {
		commandRepository.reschedule(commandId, Status.Processing, Status.Retry, LocalDateTime.now().plusSeconds(delayHeartBeat));
	}

	public void registerProcessor(String commandType, CommandProcessor processor) {
		this.processors.put(commandType, processor);
	}
	
	private void registerAfterCommit(Runnable action) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}
}
