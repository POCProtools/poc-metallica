package fr.insee.metallica.pocprotoolscommand.processor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.exception.CommandExecutionException;
import fr.insee.metallica.pocprotoolscommand.service.CommandEngine;

public abstract class AbstractCommandProcessor implements CommandProcessor {
	@Autowired
	protected CommandEngine engine;
	
	protected String commandType;

	public AbstractCommandProcessor(String commandType) {
		this.commandType = commandType;
	}
	
	@PostConstruct
	protected void register() {
		engine.registerProcessor(commandType, this);
	}

	public abstract Object process(Command command) throws CommandExecutionException;
}
