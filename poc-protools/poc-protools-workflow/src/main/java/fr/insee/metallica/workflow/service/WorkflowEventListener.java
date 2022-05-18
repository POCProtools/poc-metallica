package fr.insee.metallica.workflow.service;

import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.WorkflowStep;

public interface WorkflowEventListener {
	public static enum Type {
		Done, Error
	}
	
	public void onEvent(Workflow workflow, WorkflowStep step, Object result);
}
