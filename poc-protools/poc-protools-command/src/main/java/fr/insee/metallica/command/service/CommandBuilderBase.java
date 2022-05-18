package fr.insee.metallica.command.service;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.metallica.command.domain.Command;

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
		if (payload instanceof String) {
			return payload((String)payload); 
		}
		command.setPayload(this.commandService.mapper.writeValueAsString(payload));
		return getThis();
	}

	public T context(String context) { 
		command.setContext(context);
		return getThis();
	}
	public T context(Object context) throws JsonProcessingException {
		command.setContext(this.commandService.mapper.writeValueAsString(context));
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