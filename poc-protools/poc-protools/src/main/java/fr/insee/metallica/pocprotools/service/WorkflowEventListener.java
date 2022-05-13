package fr.insee.metallica.pocprotools.service;

import fr.insee.metallica.pocprotools.domain.Workflow;
import fr.insee.metallica.pocprotools.domain.WorkflowStep;

public interface WorkflowEventListener {
	public static enum Type {
		Done, Error
	}
	
	public void onEvent(Workflow workflow, WorkflowStep step, Object result);
}
