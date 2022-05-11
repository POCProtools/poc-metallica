package fr.insee.metallica.pocprotools.command.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.command.domain.Command.Status;
import fr.insee.metallica.pocprotools.command.repository.CommandRepository;
import fr.insee.metallica.pocprotools.command.service.CommandEventListener.Type;

@Service
public class CommandService {
	static final Logger log = LoggerFactory.getLogger(CommandService.class);
	
	@Autowired
	private CommandRepository commandRepository;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	private final Map<Type, List<CommandEventListener>> listeners = new HashMap<>();
	
	public CommandBuilder createCommand(String type) {
		return new CommandBuilder(type);
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

	@Transactional
	public Command done(Command command, Object result) throws JsonProcessingException {
		
		command = commandRepository.getById(command.getId());
		command.setStatus(Status.Done);
		if (result != null) {
			if (result instanceof String)
				command.setResult((String)result);
			else 
				command.setResult(mapper.writeValueAsString(result));
		}
		command = commandRepository.save(command);
		publish(Type.Done, command, result);
		return command;
	}

	@Transactional
	public Command error(Command command, String message) {
		command = commandRepository.getById(command.getId());
		command.setStatus(Status.Error);
		command.setResult(message);
		command = commandRepository.save(command);
		publish(Type.Error, command, message);
		return command;
	}

	@Transactional
	public Command retry(Command command, String message, int delayInSeconds) {
		command = commandRepository.getById(command.getId());
		command.setStatus(Status.Retry);
		command.setResult(message);
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
	}
	
	public void publish(CommandEventListener.Type type, Command command) {
		listeners.getOrDefault(type, List.of()).forEach(l -> l.onEvent(command, null));
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
	
	public class CommandBuilder {
		private Command c = new Command();
		
		public CommandBuilder(String type) {
			c.setType(type);
		}
		public CommandBuilder payload(String payload) { 
			c.setPayload(payload);
			return this;
		}
		public CommandBuilder payload(Object payload) throws JsonProcessingException { 
			c.setPayload(payload == null ? null :
				payload instanceof String ? (String) payload :
				mapper.writeValueAsString(payload));
			return this;
		}
		public CommandBuilder context(String context) { 
			c.setContext(context);
			return this;

		}
		public CommandBuilder context(Object context) throws JsonProcessingException {
			c.setContext(context == null ? null :
					context instanceof String ? (String) context :
					mapper.writeValueAsString(context));
			return this;
		}
		public CommandBuilder scheduledTime(LocalDateTime date) throws JsonProcessingException {
			c.setNextScheduledTime(date);
			return this;
		}

		public Command saveAndSend() {
			c.setStatus(Status.Pending);
			if (c.getNextScheduledTime() == null)
				c.setNextScheduledTime(LocalDateTime.now());
			
			return transactionTemplate.execute((status) -> {
				c = commandRepository.save(c);
				publish(Type.Added,c, null);
				return c;
			});
		}

		public Command saveAndSendWithLimit(int limit, String limitKey) {
			c.setStatus(Status.Pending);
			c.setConcurrencyLimit(limit);
			c.setLimitKey(limitKey);
			if (c.getNextScheduledTime() == null)
				c.setNextScheduledTime(LocalDateTime.now());
			
			return transactionTemplate.execute((status) -> {
				c = commandRepository.save(c);
				publish(Type.Added,c, null);
				return c;
			});
		}

		public Command saveNoSend() {
			c.setStatus(Status.Pending);
			if (c.getNextScheduledTime() == null)
				c.setNextScheduledTime(LocalDateTime.now());
			return transactionTemplate.execute((status) -> {
				c = commandRepository.save(c);
				return c;
			});
		}
	}
}
