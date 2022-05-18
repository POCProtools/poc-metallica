package fr.insee.metallica.command.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insee.metallica.command.processor.CommandProcessor;

public class CommandProcessorService {
	static final Logger log = LoggerFactory.getLogger(CommandProcessorService.class);
	
	private final Map<String, CommandProcessor> processors = new HashMap<>();
	
	public void registerProcessor(String commandType, CommandProcessor processor) {
		this.processors.put(commandType, processor);
	}
	
	public CommandProcessor getProcessor(String type) {
		return processors.get(type);
	}
}
