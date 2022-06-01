package fr.insee.metallica.pocprotools.processor;

import java.util.HashMap;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.processor.TypedAbstractCommandProcessor;

@Service
public class TriggerCommandProcessor extends TypedAbstractCommandProcessor<TriggerCommandPayload> {
	@Autowired
	private RuntimeService runtimeService;
	
	public TriggerCommandProcessor() {
		super("Trigger", TriggerCommandPayload.class);
	}

	@Override
	public Object process(Command command, TriggerCommandPayload payload) throws CommandExecutionException {
		var map = new HashMap<String,Object>();
		map.put(payload.getWorkflowName() + "Status", payload.getWorkflowStatus());
		map.put(payload.getWorkflowName() + "Result", payload.getWorkflowResult());
		
		runtimeService.trigger(payload.getExecutionId(), map);
		return null;
	}

}
