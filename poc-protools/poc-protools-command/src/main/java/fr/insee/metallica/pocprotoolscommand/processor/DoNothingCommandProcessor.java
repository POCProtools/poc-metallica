package fr.insee.metallica.pocprotoolscommand.processor;

import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.exception.CommandExecutionException;

public class DoNothingCommandProcessor extends AbstractCommandProcessor {
	public DoNothingCommandProcessor() {
		super(Processors.DoNothing);
	}

	@Override
	public Object process(Command command) throws CommandExecutionException {
		return null;
	}
}
