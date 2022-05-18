package fr.insee.metallica.command.processor;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;

public class DoNothingCommandProcessor extends AbstractCommandProcessor {
	public DoNothingCommandProcessor() {
		super(Processors.DoNothing);
	}

	@Override
	public Object process(Command command) throws CommandExecutionException {
		return null;
	}
}
