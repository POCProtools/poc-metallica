package fr.insee.metallica.pocprotoolscommand.exception;

public class CommandExecutionAbortException extends CommandExecutionException {
	private static final long serialVersionUID = 1L;
	
	public CommandExecutionAbortException(String message, Throwable t) {
		super(message, t);
	}
	
	public CommandExecutionAbortException(String message) {
		super(message);
	}
}
