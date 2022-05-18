package fr.insee.metallica.command.processor;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;

public interface CommandProcessor {
	Object process(Command command) throws CommandExecutionException;
	
	default boolean isResultSerialized() { return false; }
	
	default boolean isAsynchronousResult() { return false; }
	
	default Command getAsyncResultCommand(Command command) { return null; }
}
