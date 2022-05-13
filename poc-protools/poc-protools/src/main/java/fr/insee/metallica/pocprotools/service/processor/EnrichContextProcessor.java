package fr.insee.metallica.pocprotools.service.processor;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.pocprotools.repository.WorkflowRepository;
import fr.insee.metallica.pocprotools.service.WorkflowService;
import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.exception.CommandExecutionAbortException;
import fr.insee.metallica.pocprotoolscommand.exception.CommandExecutionException;
import fr.insee.metallica.pocprotoolscommand.processor.TypedAbstractCommandProcessor;

@Component
public class EnrichContextProcessor extends TypedAbstractCommandProcessor<ObjectNode> {
	public final static String Name = "EnrichContextProcessor";
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private WorkflowRepository workflowRepository;
	
	public EnrichContextProcessor() {
		super(Name, ObjectNode.class);
	}

	@Override
	@Transactional
	public Object process(Command command, ObjectNode payload) throws CommandExecutionException {
		try {
			var metadata = workflowService.getMetadatas(command);
			var workflow = workflowRepository.findById(metadata.getWorkflowId())
					.orElseThrow(() -> new CommandExecutionAbortException("Could not load workflow " + metadata.getWorkflowId()));
			var context = workflowService.deserialize(workflow.getContext());
			var it = payload.fields();
			while (it.hasNext()) {
				var node = it.next();
				context.set(node.getKey(), node.getValue());
			}
			workflow.setContext(workflowService.serialize(context));
			workflowRepository.save(workflow);
			return null;
		} catch (JsonProcessingException e) {
			throw new CommandExecutionAbortException("Serialization problem in enrich service", e);
		}
	}

}
