package fr.insee.metallica.pocprotools.processor;

import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.Workflow.Status;

public class TriggerCommandPayload {
	private String executionId;
	private String workflowName;
	private Workflow.Status workflowStatus;
	private Object workflowResult;
	
	public TriggerCommandPayload() {
	}

	public TriggerCommandPayload(String executionId, String workflowName, Status workflowStatus, Object workflowResult) {
		this.executionId = executionId;
		this.workflowName = workflowName;
		this.workflowStatus = workflowStatus;
		this.workflowResult = workflowResult;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public Workflow.Status getWorkflowStatus() {
		return workflowStatus;
	}

	public void setWorkflowStatus(Workflow.Status workflowStatus) {
		this.workflowStatus = workflowStatus;
	}

	public Object getWorkflowResult() {
		return workflowResult;
	}

	public void setWorkflowResult(Object workflowResult) {
		this.workflowResult = workflowResult;
	}
	
}