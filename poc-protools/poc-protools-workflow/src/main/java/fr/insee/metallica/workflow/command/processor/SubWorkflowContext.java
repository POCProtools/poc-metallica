package fr.insee.metallica.workflow.command.processor;

import com.fasterxml.jackson.databind.node.ObjectNode;

class SubWorkflowContext {
	private String workflowName;
	private ObjectNode context;
	
	public ObjectNode getContext() {
		return context;
	}
	public void setContext(ObjectNode context) {
		this.context = context;
	}
	public String getWorkflowName() {
		return workflowName;
	}
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
}