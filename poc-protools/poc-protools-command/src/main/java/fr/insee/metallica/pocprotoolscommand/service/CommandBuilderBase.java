package fr.insee.metallica.pocprotoolscommand.service;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.metallica.pocprotoolscommand.domain.Command;

public abstract class CommandBuilderBase<T> {
	protected final CommandService commandService;
	protected Command command = new Command();
	
	protected abstract T getThis();
	
	public CommandBuilderBase(CommandService commandService, String type) {
		this.commandService = commandService;
		command.setType(type);
	}
	public T payload(String payload) { 
		command.setPayload(payload);
		return getThis();
	}
	public T payload(Object payload) throws JsonProcessingException { 
		command.setPayload(payload == null ? null :
			payload instanceof String ? (String) payload :
			this.commandService.mapper.writeValueAsString(payload));
		return getThis();
	}
	public T context(String context) { 
		command.setContext(context);
		return getThis();
	}
	public T context(Object context) throws JsonProcessingException {
		command.setContext(context == null ? null :
				context instanceof String ? (String) context :
				this.commandService.mapper.writeValueAsString(context));
		return getThis();
	}
	public T scheduledTime(LocalDateTime date) throws JsonProcessingException {
		command.setNextScheduledTime(date);
		return getThis();
	}
	public T rescheduledDelay(int delayInSeconds) throws JsonProcessingException {
		command.setRescheduledDelay(delayInSeconds);
		return getThis();
	}
}