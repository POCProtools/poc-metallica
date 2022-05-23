package fr.insee.metallica.pocprotools.processor;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.processor.AbstractCommandProcessor;

@Service
public class SendActivitySignalProcessor extends AbstractCommandProcessor {
	@Autowired
	private RuntimeService runtimeService;
	
	public SendActivitySignalProcessor() {
		super("ActivitySignal");
	}
	
	@Override
	public Object process(Command command) throws CommandExecutionException {
		command.getContext();
		return null;
	}

}
