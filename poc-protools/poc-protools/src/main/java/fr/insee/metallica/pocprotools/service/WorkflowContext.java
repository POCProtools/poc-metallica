package fr.insee.metallica.pocprotools.service;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowContext {
	private UUID WorkflowId;
	private UUID StepId;
	
	public WorkflowContext() {
	}
	
	public WorkflowContext(UUID workflowId, UUID stepId) {
		super();
		WorkflowId = workflowId;
		StepId = stepId;
	}

	public UUID getWorkflowId() {
		return WorkflowId;
	}
	public void setWorkflowId(UUID workflowId) {
		WorkflowId = workflowId;
	}
	public UUID getStepId() {
		return StepId;
	}
	public void setStepId(UUID stepId) {
		StepId = stepId;
	}
}