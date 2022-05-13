package fr.insee.metallica.pocprotools.service;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.metallica.pocprotools.controller.StepDescriptor;
import fr.insee.metallica.pocprotools.controller.WorkflowDescriptor;
import fr.insee.metallica.pocprotools.controller.Workflows;
import fr.insee.metallica.pocprotools.domain.Workflow;
import fr.insee.metallica.pocprotools.domain.WorkflowStep;
import fr.insee.metallica.pocprotools.domain.WorkflowStep.Status;
import fr.insee.metallica.pocprotools.repository.WorkflowStepRepository;
import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.service.CommandEventListener.Type;
import fr.insee.metallica.pocprotoolscommand.service.CommandService;

@Service
public class WorkflowEngine {
	private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private WorkflowStepRepository workflowStepRepository;
	
	@Autowired
	private SimpleTemplateService simpleTemplateService;
	
	@Autowired
	private WorkflowService workflowService;
	
	@PostConstruct
	public void init() {
		commandService.subscribe((command, body) -> {
			// this is in the command transaction
			try {
				var metadata = workflowService.getMetadatas(command);
				if (metadata == null) {
					// this is not a command from a workflow we should not process it
					return;
				}
				var step = workflowStepRepository.findById(metadata.getStepId()).orElseThrow();
				
				var workflowDescriptor = Workflows.Workflows.get(step.getWorkflow().getWorkflowId());
				var stepDescriptor = workflowDescriptor.getStep(step.getStepId());
				
				if (command.getStatus() == Command.Status.Error) {
					step.setStatus(Status.Error);
					workflowService.error(step.getWorkflow(), step, body);
				} else if (command.getStatus() == Command.Status.Retry) {
					step.setStatus(Status.Retry);
				} else if (command.getStatus() == Command.Status.Processing) {
					step.setStatus(Status.Running);
				} else {
					step.setStatus(Status.Success);
					if (!stepDescriptor.isFinalStep()) {
						var nextStep = workflowService.createStep(step.getWorkflow(), stepDescriptor.getNextStep());
						startStep(nextStep, command.getResult());
					} else {
						workflowService.done(step.getWorkflow(), step, command.getResult());
					}
				}
				workflowStepRepository.save(step);
			} catch (Exception e) {
				log.error("Could not process workflow as metadata are not retrievable from context", e);
			}
		}, Type.Done, Type.Error, Type.Retry, Type.Aquired );
	}
	
	@Transactional
	public CompletableFuture<Object> startWorkflowAndWait(WorkflowDescriptor descriptor, Object context) throws JsonProcessingException {
		var workflow = workflowService.createWorkflow(descriptor, context);
		var step = workflowService.createStep(workflow, descriptor.getInitialStep());
		var future = new CompletableFuture<>();
		
		workflowService.subscribe(workflow.getId(), (finalWorkflow, finalStep, result) -> {
			if (finalWorkflow.getStatus() == Workflow.Status.Error) {
				future.completeExceptionally(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing Step : " + workflowService.getStepDescriptor(finalStep).getLabel()));
			} else if (finalWorkflow.getStatus() == Workflow.Status.Success) {
				future.completeAsync(() -> result);
			}
		});
		
		startStep(step);
		return future;
	}
	
	@Transactional
	@Async
	public Future<Workflow> startWorkflow(WorkflowDescriptor descriptor, Object context) throws JsonProcessingException {
		var workflow = workflowService.createWorkflow(descriptor, context);
		var step = workflowService.createStep(workflow, descriptor.getInitialStep());
		startStep(step);
		return new AsyncResult<>(workflow);
	}
	
	public void startStep(WorkflowStep step) throws JsonProcessingException {
		startStep(step, null);
	}
	
	public void startStep(WorkflowStep step, String previousStepResult) throws JsonProcessingException {
		var workflowDescriptor = workflowService.getWorkflowDescriptor(step.getWorkflow());
		var stepDescriptor = workflowService.getStepDescriptor(step);
		var context = workflowService.deserialize(step.getContext());
		context = workflowService.merge(context, "previousResult", previousStepResult);
		
		startStep(step, stepDescriptor, workflowDescriptor, context);
	}

	
	private void startStep(WorkflowStep step, StepDescriptor stepDescriptor, WorkflowDescriptor workflowDescriptor, Object context) throws JsonProcessingException {
		var metadatas = new HashMap<>();
		for (var entry : stepDescriptor.getMetadatas().entrySet()) {
			var value = entry.getValue();
			if (value instanceof String) {
				metadatas.put(entry.getKey(), simpleTemplateService.evaluateTemplate((String) value, context, metadatas));
			} else {
				metadatas.put(entry.getKey(), entry.getValue());
			}
		}
		
		var payload = simpleTemplateService.evaluateTemplate(stepDescriptor.getPayloadTemplate(), context, metadatas);
		
		commandService.createCommand(stepDescriptor.getType())
			.payload(payload)
			.context(
				new WorkflowContext(
					step.getWorkflow().getId(),
					step.getId()
				)
			).saveAndSendWithLimit(stepDescriptor.getLimit(), stepDescriptor.getLimitKey());
	}
}
