package fr.insee.metallica.pocprotools.connector;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.engine.delegate.BpmnError;

import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

public abstract class AbstractStartWorkflow implements Connector {
	private final WorkflowExecutionService workflowExecutionService;
	
	public AbstractStartWorkflow(WorkflowExecutionService workflowExecutionService) {
		this.workflowExecutionService = workflowExecutionService;
	}
	
	public abstract WorkflowDescriptor getDescriptor();

	@Override
	public IntegrationContext apply(IntegrationContext t) {
		try {
			t.getInBoundVariables().put("processInstanceId", t.getProcessInstanceId());
			t.getInBoundVariables().put("activityId", t.getClientId());
			var result = workflowExecutionService.startWorkflow(getDescriptor().getName(), t.getInBoundVariables());
			if (result != null) {
				t.getOutBoundVariables().put("result", result);
			}
			return t;
		} catch (Exception e) {
			throw new BpmnError(e.getMessage());
		}
	}

}
