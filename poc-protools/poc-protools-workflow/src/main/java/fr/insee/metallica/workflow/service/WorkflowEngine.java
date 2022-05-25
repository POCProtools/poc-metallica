package fr.insee.metallica.workflow.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.service.CommandEventListener.Type;
import fr.insee.metallica.command.service.CommandService;
import fr.insee.metallica.workflow.configuration.descriptor.StepDescriptor;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.WorkflowStep;
import fr.insee.metallica.workflow.domain.WorkflowStep.Status;
import fr.insee.metallica.workflow.repository.WorkflowRepository;
import fr.insee.metallica.workflow.repository.WorkflowStepRepository;

public class WorkflowEngine {
	private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WorkflowRepository workflowRepository;
	
	@Autowired
	private WorkflowConfigurationService workflowConfigurationService;
	
	@Autowired
	private WorkflowStepRepository workflowStepRepository;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private SimpleTemplateService simpleTemplateService;
	
	private final Map<UUID, List<WorkflowEventListener>> listeners = new HashMap<>();
	
	private final Map<Workflow.Status, List<WorkflowEventListener>> statusListeners = new HashMap<>();
	
	@PostConstruct
	public void init() {
		commandService.subscribe((command, body) -> {
			// this is in the command transaction
			try {
				var metadata = getMetadatas(command);
				if (metadata == null) {
					// this is not a command from a workflow we should not process it
					return;
				}
				var step = workflowStepRepository.findById(metadata.getStepId()).orElse(null);
				if (step == null) {
					// this may happend if this is a command to asynchronously fetch the result of a step
					return;
				}
					
				var workflowDescriptor = workflowConfigurationService.getWorkflow(step.getWorkflow().getWorkflowId());
				if (workflowDescriptor == null) {
					log.error("Workflow with id {} introuvable", step.getWorkflow().getWorkflowId());
					return;
				}
				var stepDescriptor = workflowDescriptor.getStep(step.getStepId());
				if (stepDescriptor == null) {
					log.error("Step with id {} introuvable", step.getStepId());
					return;
				}
				
				if (command.getStatus() == Command.Status.Error) {
					step.setStatus(Status.Error);
					error(step.getWorkflow(), step, (body instanceof String) ? (String) body : mapper.writeValueAsString(body));
				} else if (command.getStatus() == Command.Status.Retry) {
					step.setStatus(Status.Retry);
				} else if (command.getStatus() == Command.Status.Processing) {
					step.setStatus(Status.Running);
				} else {
					step.setStatus(Status.Success);
					if (!stepDescriptor.isFinalStep()) {
						var nextStep = createStep(step.getWorkflow(), stepDescriptor.getNextStep());
						startStep(nextStep, command.getResult());
					} else {
						done(step.getWorkflow(), step, command.getResult());
					}
				}
				workflowStepRepository.save(step);
			} catch (Exception e) {
				log.error("Could not process workflow as metadata are not retrievable from context", e);
			}
		}, Type.Done, Type.Error, Type.Retry);
	}

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
		return mapper.writeValueAsString(context);
	}

	public ObjectNode deserialize(String context) throws JsonProcessingException {
		return mapper.readValue(context, ObjectNode.class);
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
		return workflowConfigurationService.getWorkflow(workflow.getWorkflowId());
	}
	
	public StepDescriptor getStepDescriptor(WorkflowStep step) {
		return getWorkflowDescriptor(step.getWorkflow()).getStep(step.getStepId());
	}

	public ObjectNode merge(ObjectNode context, String key, String serializedObject) throws JsonProcessingException {
		if (serializedObject == null) {
			return context;
		}
		context.set(key, deserialize(serializedObject, JsonNode.class));
		return context;
	}

	@Transactional
	public void done(Workflow workflow, WorkflowStep step, String serializedResult) {
		workflow.setStatus(Workflow.Status.Success);
		this.workflowRepository.save(workflow);
		log.info("The workflow {} {} ended with a sucess", workflowConfigurationService.getWorkflow(workflow.getWorkflowId()).getName(), workflow.getId());
		publish(workflow, step, serializedResult);
	}

	@Transactional
	public void error(Workflow workflow, WorkflowStep step, String message) {
		workflow.setStatus(Workflow.Status.Error);
		this.workflowRepository.save(workflow);
		log.info("The workflow {} {} ended with an error {}", workflowConfigurationService.getWorkflow(workflow.getWorkflowId()).getName(), workflow.getId(), message);
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
	
	public void subscribe(WorkflowEventListener listener, Workflow.Status... listenedStatus) {
		synchronized (statusListeners) {
			for (var status : listenedStatus) {
				var listeners = this.statusListeners.get(status);
				if (listeners == null) {
					this.statusListeners.put(status, listeners = new LinkedList<>());
				}
				listeners.add(listener);
			}
		}
	}
	
	public void publish(Workflow workflow, WorkflowStep step, String serializedResult) {
		listeners.getOrDefault(workflow.getId(), List.of()).forEach(l -> l.onEvent(workflow, step, serializedResult));
		statusListeners.getOrDefault(workflow.getStatus(), List.of()).forEach(l -> l.onEvent(workflow, step, serializedResult));
	}
	
	
	public void startStep(WorkflowStep step) throws JsonProcessingException {
		startStep(step, null);
	}
	
	public void startStep(WorkflowStep step, String previousStepResult) throws JsonProcessingException {
		var workflowDescriptor = getWorkflowDescriptor(step.getWorkflow());
		var stepDescriptor = getStepDescriptor(step);
		var context = deserialize(step.getContext());
		context = merge(context, "previousResult", previousStepResult);
		
		startStep(step, stepDescriptor, workflowDescriptor, context);
	}
	
	private void startStep(WorkflowStep step, StepDescriptor stepDescriptor, WorkflowDescriptor workflowDescriptor, ObjectNode context) throws JsonProcessingException {
		var metadatas = new HashMap<>();
		for (var entry : stepDescriptor.getMetadatas().entrySet()) {
			var value = entry.getValue();
			if (value instanceof String) {
				metadatas.put(entry.getKey(), simpleTemplateService.evaluateTemplate((String) value, context, metadatas));
			} else {
				metadatas.put(entry.getKey(), entry.getValue());
			}
		}

		var shouldSkip = simpleTemplateService.evaluateTemplate(stepDescriptor.getSkip(), context, metadatas);
		
		if (shouldSkip != null && shouldSkip instanceof Boolean && ((Boolean)shouldSkip)) {
			if (!stepDescriptor.isFinalStep()) {
				var nextStep = createStep(step.getWorkflow(), stepDescriptor.getNextStep());
				startStep(nextStep, serialize(context.get("previousResult")));
			} else {
				done(step.getWorkflow(), step, serialize(context.get("previousResult")));
			}
		}
		
		var payload = simpleTemplateService.evaluateTemplate(stepDescriptor.getPayloadTemplate(), context, metadatas);
		
		var command = commandService
			.createCommand(stepDescriptor.getType())
			.payload(payload)
			.context(
				new WorkflowContext(
					step.getWorkflow().getId(),
					step.getId()
				)
			);
		
		if (stepDescriptor.getAsyncResult() != null) {
			var asyncResult = stepDescriptor.getAsyncResult();
			var asyncResultPayload = simpleTemplateService.evaluateTemplate(asyncResult.getPayloadTemplate(), context, metadatas);
			command.asyncResult(asyncResult.getType())
				.payload(asyncResultPayload)
				.context(
					new WorkflowContext(
						step.getWorkflow().getId(),
						asyncResult.getId()
					)
				);
		}
		command.saveAndSend(stepDescriptor.getLimit(), stepDescriptor.getLimitKey());
	}
}
