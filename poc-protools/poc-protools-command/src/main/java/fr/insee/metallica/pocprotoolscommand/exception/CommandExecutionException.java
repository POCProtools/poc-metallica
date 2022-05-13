package fr.insee.metallica.pocprotoolscommand.exception;

public class CommandExecutionException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CommandExecutionException(String message, Throwable t) {
		super(message, t);
	}
	
	public CommandExecutionException(String message) {
		super(message);
	}
}
