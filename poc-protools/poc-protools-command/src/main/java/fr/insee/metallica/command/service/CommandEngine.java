package fr.insee.metallica.command.service;

import java.time.LocalDateTime;
import java.util.HashSet;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.insee.metallica.command.configuration.CommandProperties;
import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.domain.Command.Status;
import fr.insee.metallica.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.command.exception.CommandExecutionRetryException;
import fr.insee.metallica.command.repository.CommandRepository;
import fr.insee.metallica.command.service.CommandEventListener.Type;

public class CommandEngine {
	static final Logger log = LoggerFactory.getLogger(CommandEngine.class);
	
	@Autowired
	private CommandProperties commandProperties;
	
	@Autowired
	private CommandProcessorService commandProcessorService;
	
	@Autowired
	private CommandService commandService;

	@Autowired
	private CommandRepository commandRepository;
	
	private final Set<UUID> currentCommands = new HashSet<UUID>();
	
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	@PostConstruct
	public void registerCommandEvent() {
		commandService.subscribe((command, data) -> registerAfterCommit(() -> this.startProcess(command.getId())), Type.Added);
	}
	
	public void startProcess(UUID commandId) {
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
				var processor = commandProcessorService.getProcessor(command.getType());
				if (processor == null) throw new CommandExecutionAbortException("Processor for " + command.getType() + " not found");
				
				log.info("Starting command {}", command.getId());
				commandService.publish(Type.Processing, command);
				
				var result = processor.process(command);
				commandService.done(command, result, processor.isResultSerialized());
				rescheduleWaiting(command);
				
				log.info("Command executed {}", command.getId());
			} catch (CommandExecutionAbortException e) {
				log.error("Error in command " + command.getId() + " abort", e);
				commandService.error(command, e.getMessage());
			} catch (CommandExecutionRetryException e) {
				log.info("Rescheduling of " + command.getId() + ": " + e.getMessage());
				commandService.retry(command, e.getMessage(), e.getDelayInSeconds().orElse(commandProperties.getSchedule().getDelayBeetweenRetryCheck()));
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
	
	private void rescheduleWaiting(Command command) {
		if (command.getConcurrencyLimit() <= 0) return;
		
		var page = commandRepository.findByLimitKeyAndStatus(command.getLimitKey(), Status.Retry, PageRequest.of(0, 1));
		if (page.getNumberOfElements() == 0) {
			return;
		}
		this.startProcess(page.getContent().get(0).getId());
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
		commandRepository.reschedule(commandId, Status.Processing, Status.Retry, LocalDateTime.now().plusSeconds(commandProperties.getSchedule().getDelayHeartBeat()));
	}

	private void registerAfterCommit(Runnable action) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}

	Set<UUID> getCurrentCommands() {
		return this.currentCommands;
	}
}
