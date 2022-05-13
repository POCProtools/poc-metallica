package fr.insee.metallica.pocprotoolscommand.exception;

public class CommandExecutionRetryException extends CommandExecutionException {
	private static final long serialVersionUID = 1L;
	
	private final int delayInSeconds;
	
	public CommandExecutionRetryException(String message, Throwable t, int delayInSeconds) {
		super(message, t);
		this.delayInSeconds = delayInSeconds;
	}

	public CommandExecutionRetryException(String message, int delayInSeconds) {
		super(message);
		this.delayInSeconds = delayInSeconds;
	}

	public CommandExecutionRetryException(String message, Throwable t) {
		this(message, t, 5);
	}

	public CommandExecutionRetryException(String message) {
		this(message, 5);
	}
	
	public int getDelayInSeconds() {
		return delayInSeconds;
	}
}
