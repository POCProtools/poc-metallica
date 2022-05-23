package fr.insee.metallica.pocprotools.configuration;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.insee.metallica.pocprotools.connector.AbstractStartWorkflow;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.service.WorkflowConfigurationService;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

@Configuration
public class WorkflowBeans {
	@Bean
	public Map<String, Connector> workflowConnectors(WorkflowConfigurationService workflowConfigurationService, WorkflowExecutionService workflowExecutionService, ConfigurableListableBeanFactory beanFactory) {
		var map = new HashMap<String, Connector>(); 
		workflowConfigurationService.getWorkflows().forEach(w -> {
			var singleton = new AbstractStartWorkflow(workflowExecutionService) {
				public WorkflowDescriptor getDescriptor() { return w; }
			};
			map.put(w.getName(), singleton);
			beanFactory.registerSingleton(w.getName(), singleton);
		});
		return map;
	}
}

