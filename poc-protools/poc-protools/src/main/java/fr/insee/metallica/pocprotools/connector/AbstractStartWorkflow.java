package fr.insee.metallica.pocprotools.connector;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

public abstract class AbstractStartWorkflow implements Connector {
	@Autowired
	private WorkflowExecutionService workflowExecutionService;
	
	public abstract WorkflowDescriptor getDescriptor();

	@Override
	public IntegrationContext apply(IntegrationContext t) {
		var result = workflowExecutionService.executeWorkflow(getDescriptor().getName(), t.getInBoundVariables())
			.join();
		if (result != null) {
			t.getOutBoundVariables().put("result", result);
		}
		return t;
	}

}
