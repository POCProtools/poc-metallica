package fr.insee.metallica.pocprotoolscommand.exception;

import java.util.Optional;

public class CommandExecutionRetryException extends CommandExecutionException {
	private static final long serialVersionUID = 1L;
	
	private final Optional<Integer> delayInSeconds;
	
	public CommandExecutionRetryException(String message, Throwable t, Integer delayInSeconds) {
		super(message, t);
		this.delayInSeconds =  delayInSeconds == null ? Optional.empty() : Optional.of(delayInSeconds);
	}

	public CommandExecutionRetryException(String message, Integer delayInSeconds) {
		this(message, null, delayInSeconds);
	}

	public CommandExecutionRetryException(String message, Throwable t) {
		this(message, t, null);
	}

	public CommandExecutionRetryException(String message) {
		this(message, null, null);
	}
	
	public Optional<Integer> getDelayInSeconds() {
		return delayInSeconds;
	}
}
