package fr.insee.metallica.command.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;

public class PrintCommandProcessor extends AbstractStringCommandProcessor {
	private static final Logger log = LoggerFactory.getLogger(PrintCommandProcessor.class);
	
	public PrintCommandProcessor() {
		super(Processors.Print);
	}

	@Override
	public Object process(Command command, String payload) throws CommandExecutionException {
		log.info(payload);
		return null;

	}

}
