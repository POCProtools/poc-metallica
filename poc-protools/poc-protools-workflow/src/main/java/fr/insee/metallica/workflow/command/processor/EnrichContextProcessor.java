package fr.insee.metallica.workflow.command.processor;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.processor.TypedAbstractCommandProcessor;
import fr.insee.metallica.workflow.repository.WorkflowRepository;
import fr.insee.metallica.workflow.service.WorkflowEngine;

public class EnrichContextProcessor extends TypedAbstractCommandProcessor<ObjectNode> {
	public final static String Name = "EnrichContext";
	
	@Autowired
	private WorkflowEngine workflowService;
	
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
