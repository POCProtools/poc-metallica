package fr.insee.metallica.pocprotoolscommand.processor;

import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.exception.CommandExecutionException;

public interface CommandProcessor {
	Object process(Command command) throws CommandExecutionException;
}
