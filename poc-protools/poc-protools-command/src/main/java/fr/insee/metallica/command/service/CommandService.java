package fr.insee.metallica.command.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.domain.Command.Status;
import fr.insee.metallica.command.repository.CommandRepository;
import fr.insee.metallica.command.service.CommandEventListener.Type;

public class CommandService {
	static final Logger log = LoggerFactory.getLogger(CommandService.class);
	
	@Autowired 
	CommandRepository commandRepository;

	@Autowired 
	CommandProcessorService commandProcessorService;

	@Autowired 
	TransactionTemplate transactionTemplate;

	@Autowired 
	ObjectMapper mapper;
	
	private final Map<Type, List<CommandEventListener>> listeners = new HashMap<>();
	
	private final Map<UUID, Map<Type, List<CommandEventListener>>> commandListeners = new HashMap<>();
	
	@PostConstruct
	void init() {
		subscribe((Command command, Object result) -> { 
			if (command.getOriginalCommand() != null) {
				command.getOriginalCommand().setStatus(Status.Done);
				command.getOriginalCommand().setResult(command.getResult());
				commandRepository.save(command.getOriginalCommand());	
				publish(Type.Done, command.getOriginalCommand(), result);
			}
		}, Type.Done);
		subscribe((Command command, Object result) -> { 
			if (command.getOriginalCommand() != null) {
				command.getOriginalCommand().setStatus(Status.Error);
				command.getOriginalCommand().setResult(command.getResult());
				commandRepository.save(command.getOriginalCommand());	
				publish(Type.Error, command.getOriginalCommand(), result);
			}
		}, Type.Error);
	}
	
	public CommandBuilder createCommand(String type) {
		return new CommandBuilder(this, commandProcessorService, type);
	}
	
	public Command aquireOneToProcess() {
		Command command;
		do {
			command = commandRepository.getOneCommandToRun();
			if (command == null) break;
		} while (aquireToProcess(command.getId()) == null);

		return command;
	}
	
	public Command aquireToProcess(UUID commandId) {
		if (commandRepository.setStatus(commandId, List.of(Status.Pending, Status.Retry), Status.Processing) == 0) {
			return null;
		}

		var c = commandRepository.findById(commandId).orElse(null);
		if (c != null) {
			publish(Type.Aquired, c, null);
			heartBeat(c.getId());
		}
		return c;
	}
	
	public void subscribe(CommandEventListener listener, CommandEventListener.Type ...types) {
		synchronized (listeners) {
			for (var type : types) {
				var typeListeners = listeners.get(type);
				if (typeListeners == null) {
					listeners.put(type, typeListeners = new LinkedList<>());
				}
				typeListeners.add(listener);
			}
		}
	}
	
	public void subscribe(CommandEventListener listener, UUID commandId, CommandEventListener.Type ...types) {
		synchronized (commandListeners) {
			var listeners = commandListeners.get(commandId);
			if (listeners == null) {
				commandListeners.put(commandId, listeners = new HashMap<>());
			}
			for (var type : types) {
				var typeListeners = listeners.get(type);
				if (typeListeners == null) {
					listeners.put(type, typeListeners = new LinkedList<>());
				}
				typeListeners.add(listener);
			}
		}
	}

	@Transactional
	public Command done(Command command, Object result, boolean isResultSerialized) throws JsonProcessingException {
		command = commandRepository.getById(command.getId());
		Type publish = Type.Done;
		if (result != null) {
			if (result instanceof String && isResultSerialized) {
				command.setResult((String) result);
			} else {
				command.setResult(mapper.writeValueAsString(result));
			}
		}
		if (command.getResultFetcher() != null) {
			command.getResultFetcher().setStatus(Status.Pending);
			command.getResultFetcher().setNextScheduledTime(LocalDateTime.now());
			commandRepository.save(command.getResultFetcher());
			command.setStatus(Status.AwaitingResult);
			publish = Type.AwaitingResult;
		} else {
			command.setStatus(Status.Done);
		}
		command = commandRepository.save(command);
		publish(publish, command, result);
		commandListeners.remove(command.getId());
		return command;
	}

	@Transactional
	public Command error(Command command, String message) {
		command = commandRepository.getById(command.getId());
		command.setStatus(Status.Error);
		try {
			command.setResult(mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			command.setResult("\"Could not serialize error message\"");
		}
		command = commandRepository.save(command);
		if (command.getResultFetcher() != null) {
			command.getResultFetcher().setStatus(Status.Error);
			commandRepository.save(command.getResultFetcher());
		} else {
			command.setStatus(Status.Error);
		}
		publish(Type.Error, command, message);
		commandListeners.remove(command.getId());
		return command;
	}

	@Transactional
	public Command retry(Command command, String message, int delayInSeconds) {
		command = commandRepository.getById(command.getId());
		command.setStatus(Status.Retry);
		try {
			command.setResult(mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			command.setResult("\"Could not serialize retry error message\"");
		}
		command.setNbTry(command.getNbTry() + 1);
		command.setNextScheduledTime(LocalDateTime.now().plusSeconds(delayInSeconds));
		command = commandRepository.save(command);
		publish(Type.Retry, command, message);
		return command;
	}
	
	public void heartBeat(UUID commandId) {
		commandRepository.heartBeat(commandId);
	}
	
	public void publish(CommandEventListener.Type type, Command command, Object result) {
		listeners.getOrDefault(type, List.of()).forEach(l -> {
			try {
				l.onEvent(command, result);
			} catch (Exception e) {
				log.error("Exception thrown in subscriber", e);
			}
		});
		commandListeners.getOrDefault(command.getId(), Map.of()).getOrDefault(type, List.of()).forEach(l -> {
			try {
				l.onEvent(command, result);
			} catch (Exception e) {
				log.error("Exception thrown in subscriber", e);
			}
		});
	}
	
	public void publish(CommandEventListener.Type type, Command command) {
		publish(type, command, null);
	}
	
	public void publishAfterCommit(CommandEventListener.Type type, Command command) {
		registerAfterCommit(() -> publish(type, command));
	}
	
	public void publishAfterCommit(CommandEventListener.Type type, Command command, Object result) {
		registerAfterCommit(() -> publish(type, command, result));
	}
	
	private void registerAfterCommit(Runnable action) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}

	public Command executeInTransaction(Command command, Command asyncResult) {
		return transactionTemplate.execute((status) -> {
			var processor = commandProcessorService.getProcessor(command.getType());
			if (asyncResult != null) {
				asyncResult.setStatus(Status.WaitingToBeScheduled);
				commandRepository.save(asyncResult);
				command.setResultFetcher(asyncResult);
			} else if (processor != null && processor.isAsynchronousResult()) {
				var asyncResultCommand = processor.getAsyncResultCommand(command); 
				asyncResultCommand.setStatus(Status.WaitingToBeScheduled);
				commandRepository.save(asyncResultCommand);
				command.setResultFetcher(asyncResultCommand);
			}
			
			var c = commandRepository.save(command);
			publish(Type.Added,c, null);
			return c;
		});
	}
}
