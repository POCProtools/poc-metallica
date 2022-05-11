package fr.insee.metallica.pocprotools.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.controller.StepDescriptor;
import fr.insee.metallica.pocprotools.controller.WorkflowDescriptor;
import fr.insee.metallica.pocprotools.controller.Workflows;
import fr.insee.metallica.pocprotools.domain.Workflow;
import fr.insee.metallica.pocprotools.domain.WorkflowStep;
import fr.insee.metallica.pocprotools.domain.WorkflowStep.Status;
import fr.insee.metallica.pocprotools.repository.WorkflowRepository;
import fr.insee.metallica.pocprotools.repository.WorkflowStepRepository;

@Service
public class WorkflowService {
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WorkflowRepository workflowRepository;
	
	@Autowired
	private WorkflowStepRepository workflowStepRepository;
	
	private final Map<UUID, List<WorkflowEventListener>> listeners = new HashMap<>();

	public WorkflowContext getMetadatas(Command command) throws JsonProcessingException {
		if (command.getContext() == null) {
			return null;
		}
		return mapper.readValue(command.getContext(), WorkflowContext.class);
	}

	@Transactional
	public Workflow createWorkflow(WorkflowDescriptor descriptor, Object context) throws JsonProcessingException {
		var workflow = new Workflow();
		workflow.setWorkflowId(descriptor.getId());
		workflow.setContext(serialize(context));
		workflow.setStatus(Workflow.Status.Running);
		workflow = workflowRepository.save(workflow);
		return workflow;
	}

	public String serialize(Object context) throws JsonProcessingException {
		return context instanceof String ? (String) context : mapper.writeValueAsString(context);
	}

	public ObjectNode deserialize(String context) throws JsonProcessingException {
		return deserialize(context, ObjectNode.class);
	}

	public <T> T deserialize(String context, Class<T> clazz) throws JsonProcessingException {
		return context == null ? null : mapper.readValue(context, clazz);
	}

	public WorkflowStep createStep(Workflow workflow, StepDescriptor descriptor) {
		var step = new WorkflowStep();
		step.setStatus(Status.Pending);
		step.setStepId(descriptor.getId());
		step.setWorkflow(workflow);
		step.setContext(workflow.getContext());
		return workflowStepRepository.save(step);		
	}
	
	public WorkflowDescriptor getWorkflowDescriptor(Workflow workflow) {
		return Workflows.Workflows.get(workflow.getWorkflowId());
	}
	
	public StepDescriptor getStepDescriptor(WorkflowStep step) {
		return getWorkflowDescriptor(step.getWorkflow()).getStep(step.getStepId());
	}

	public ObjectNode merge(ObjectNode context, String key, String serializedObject) throws JsonProcessingException {
		if (serializedObject == null) {
			return context;
		}
		context.set(key, deserialize(serializedObject));
		return context;
	}

	@Transactional
	public void done(Workflow workflow, WorkflowStep step, Object result) {
		workflow.setStatus(Workflow.Status.Success);
		this.workflowRepository.save(workflow);
		publish(workflow, step, result);
	}

	@Transactional
	public void error(Workflow workflow, WorkflowStep step, Object message) {
		workflow.setStatus(Workflow.Status.Error);
		this.workflowRepository.save(workflow);
		publish(workflow, step, message);
	}
	
	public void subscribe(UUID workflowId, WorkflowEventListener listener) {
		synchronized (listeners) {
			var listeners = this.listeners.get(workflowId);
			if (listeners == null) {
				this.listeners.put(workflowId, listeners = new LinkedList<>());
			}
			listeners.add(listener);
		}
	}
	
	public void publish(Workflow workflow, WorkflowStep step, Object result) {
		listeners.getOrDefault(workflow.getId(), List.of()).forEach(l -> l.onEvent(workflow, step, result));
	}


}
