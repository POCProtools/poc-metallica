package fr.insee.metallica.command.processor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.service.CommandProcessorService;

public abstract class AbstractCommandProcessor implements CommandProcessor {
	@Autowired
	protected CommandProcessorService commandProcessorService;
	
	protected String commandType;

	public AbstractCommandProcessor(String commandType) {
		this.commandType = commandType;
	}
	
	@PostConstruct
	protected void register() {
		commandProcessorService.registerProcessor(commandType, this);
	}

	public abstract Object process(Command command) throws CommandExecutionException;
}
