package fr.insee.metallica.command.service;

import fr.insee.metallica.command.domain.Command;

public interface CommandEventListener {
	public static enum Type {
		Added, Aquired, Processing, Done, Error, Retry, AwaitingResult
	}
	
	public void onEvent(Command command, Object result);
}
