package fr.insee.metallica.workflow.service;

import java.util.concurrent.CompletableFuture;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.exception.WorkflowExecutionException;

public class WorkflowExecutionService {
	private static final Logger log = LoggerFactory.getLogger(WorkflowExecutionService.class);
	
	@Autowired
	private WorkflowEngine workflowEngine;
	
	@Autowired
	private WorkflowConfigurationService workflowConfigurationService;
	
	@SuppressWarnings("unchecked")
	@Transactional
	public <T> CompletableFuture<T> executeWorkflow(String workflowName, Object context) {
		var result = new CompletableFuture<T>();
		
		try {
			var descriptor = workflowConfigurationService.getWorkflow(workflowName);
			var workflow = workflowEngine.createWorkflow(descriptor, context);
			var step = workflowEngine.createStep(workflow, descriptor.getInitialStep());
			
			workflowEngine.subscribe(workflow.getId(), (w, lastStep, workflowResult) -> {
				if (w.getStatus() == Workflow.Status.Error) {
					result.completeExceptionally(new WorkflowExecutionException(
							"Error in workflow " + w.getId() +
							" executing step " + workflowEngine.getStepDescriptor(lastStep).getLabel() +
							": " + workflowResult)
					);
				} else if (w.getStatus() == Workflow.Status.Success) {
					result.completeAsync(() -> (T) result);
				}
			});
			
			workflowEngine.startStep(step);
			log.info("Starting workflow {}", descriptor.getName());
		} catch (JsonProcessingException e) {
			result.completeExceptionally(e);
			return result;
		}
		return result;
	}
	
	@Transactional
	public Workflow startWorkflow(String workflowName, Object context) throws JsonProcessingException {
		var descriptor = workflowConfigurationService.getWorkflow(workflowName);
		var workflow = workflowEngine.createWorkflow(descriptor, context);
		var step = workflowEngine.createStep(workflow, descriptor.getInitialStep());
		workflowEngine.startStep(step);
		log.info("Starting workflow {}", descriptor.getName());
		return workflow;
	}

}
