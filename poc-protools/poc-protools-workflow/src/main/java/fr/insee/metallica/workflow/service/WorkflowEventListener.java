package fr.insee.metallica.workflow.service;

import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.WorkflowStep;

public interface WorkflowEventListener {
	public void onEvent(Workflow workflow, WorkflowStep step, String serializedResult);
}
