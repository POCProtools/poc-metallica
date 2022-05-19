package fr.insee.metallica.workflow.command.processor;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

class MultipleSubWorkflowContext {
	private String workflowName;
	private List<JsonNode> contexts;
	
	public List<JsonNode> getContexts() {
		return contexts;
	}
	public void setContexts(List<JsonNode> context) {
		this.contexts = context;
	}
	public String getWorkflowName() {
		return workflowName;
	}
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
}