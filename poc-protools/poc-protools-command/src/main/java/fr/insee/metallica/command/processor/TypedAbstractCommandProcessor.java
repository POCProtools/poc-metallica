package fr.insee.metallica.command.processor;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.command.exception.CommandExecutionException;

public abstract class TypedAbstractCommandProcessor<TPayload> extends AbstractCommandProcessor {
	@Autowired
	private ObjectMapper mapper;

	private final Class<TPayload> payloadType;
	
	public TypedAbstractCommandProcessor(String commandType, Class<TPayload> payloadType) {
		super(commandType);
		
		this.payloadType = payloadType;
	}

	public Object process(Command command) throws CommandExecutionException {
		try {
			var payload = mapper.readValue(command.getPayload(), payloadType);
			return process(command, payload);
		} catch (JsonProcessingException e) {
			throw new CommandExecutionAbortException("Could not deserialize body", e);
		}
	}

	public abstract Object process(Command command, TPayload payload) throws CommandExecutionException;
}
