package fr.insee.metallica.workflow.exception;

public class WorkflowExecutionException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public WorkflowExecutionException(String reason) {
		super(reason);
	}
}
