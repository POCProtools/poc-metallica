package fr.insee.metallica.pocprotoolscommand.service;

import fr.insee.metallica.pocprotoolscommand.domain.Command;

public interface CommandEventListener {
	public static enum Type {
		Added, Aquired, Processing, Done, Error, Retry
	}
	
	public void onEvent(Command command, Object result);
}
