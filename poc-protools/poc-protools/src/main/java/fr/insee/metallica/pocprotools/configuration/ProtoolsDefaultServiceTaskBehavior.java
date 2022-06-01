package fr.insee.metallica.pocprotools.configuration;

import static org.activiti.runtime.api.impl.MappingExecutionContext.buildMappingExecutionContext;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.runtime.api.connector.DefaultServiceTaskBehavior;
import org.activiti.runtime.api.connector.IntegrationContextBuilder;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.springframework.context.ApplicationContext;

import fr.insee.metallica.pocprotools.connector.AbstractStartWorkflow;
import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.Workflow.Status;

public class ProtoolsDefaultServiceTaskBehavior extends DefaultServiceTaskBehavior {
	private static final long serialVersionUID = 1L;
	
	private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private VariablesMappingProvider outboundVariablesProvider;

    public ProtoolsDefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder,
                                      VariablesMappingProvider outboundVariablesProvider) {
    	super(applicationContext, integrationContextBuilder, outboundVariablesProvider);
        this.applicationContext = applicationContext;
        this.integrationContextBuilder = integrationContextBuilder;
        this.outboundVariablesProvider = outboundVariablesProvider;
    }

    /**
     * We have two different implementation strategy that can be executed
     * in according if we have a connector action definition match or not.
     **/
    @Override
    public void execute(DelegateExecution execution) {
        Connector connector = getConnector(getImplementation(execution));
        var integrationContext = integrationContextBuilder.from(execution);
        integrationContext.getInBoundVariables().put("executionId", execution.getId());
        integrationContext = connector.apply(integrationContext);
        
        if (!(connector instanceof AbstractStartWorkflow)) {
        	complete(execution, integrationContext);
        }
    }
    
    @Override
    public void trigger(DelegateExecution execution, String signalName, Object signalData) {
        Connector connector = getConnector(getImplementation(execution));
        IntegrationContext integrationContext = integrationContextBuilder.from(execution);
        
        if (connector instanceof AbstractStartWorkflow) {
        	var status = (Workflow.Status) integrationContext.getInBoundVariables().getOrDefault(getImplementation(execution) + "Status", null);
        	if (status == Status.Error) {
        		System.out.println("Propagation error");
        		ErrorPropagation.propagateError("error", execution);
        	} else {
        		complete(execution, integrationContext);
        	}
        } else {
        	super.trigger(execution, signalName, signalData);
        }
    }

	private void complete(DelegateExecution execution, IntegrationContext integrationContext) {
		execution.setVariables(
				outboundVariablesProvider.calculateOutPutVariables(
						buildMappingExecutionContext(execution),
		                integrationContext.getOutBoundVariables()
		        )
		);
		leave(execution);
	}

    private String getImplementation(DelegateExecution execution) {
        return ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
    }

    private Connector getConnector(String implementation) {
        return applicationContext.getBean(implementation,
                                          Connector.class);
    }

    private String getServiceTaskImplementation(DelegateExecution execution) {
        return ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
    }

    public boolean hasConnectorBean(DelegateExecution execution) {
        String implementation = getServiceTaskImplementation(execution);
        return applicationContext.containsBean(implementation) && applicationContext.getBean(implementation) instanceof Connector;
    }
}