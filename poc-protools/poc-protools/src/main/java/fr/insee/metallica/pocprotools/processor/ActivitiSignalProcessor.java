package fr.insee.metallica.pocprotools.processor;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.processor.TypedAbstractCommandProcessor;

@Service
public class ActivitiSignalProcessor extends TypedAbstractCommandProcessor<ActivitiEventPayload> {
	@Autowired
	private RuntimeService runtimeService;
	
	public ActivitiSignalProcessor() {
		super("ActivitiSignal", ActivitiEventPayload.class);
	}
	
	@Override
	public Object process(Command command, ActivitiEventPayload payload) {
		runtimeService.signalEventReceived(payload.getName(), payload.getContext());
		return null;
	}

}
