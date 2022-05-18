package fr.insee.metallica.command.processor;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;

public interface CommandProcessor {
	Object process(Command command) throws CommandExecutionException;
}
