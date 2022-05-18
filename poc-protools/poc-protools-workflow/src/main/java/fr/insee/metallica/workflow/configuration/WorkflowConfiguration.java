package fr.insee.metallica.workflow.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.command.configuration.EnableCommand;
import fr.insee.metallica.workflow.command.processor.EnrichContextProcessor;
import fr.insee.metallica.workflow.command.processor.SubWorkflowLauncherProcessor;
import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.repository.WorkflowRepository;
import fr.insee.metallica.workflow.service.SimpleTemplateService;
import fr.insee.metallica.workflow.service.WorkflowConfigurationService;
import fr.insee.metallica.workflow.service.WorkflowEngine;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;
import fr.insee.metallica.workflow.service.WorkflowStatusService;

@EnableTransactionManagement
@EnableCommand
@EntityScan(basePackageClasses = Workflow.class)
@EnableJpaRepositories(basePackageClasses = WorkflowRepository.class)
public class WorkflowConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public ObjectMapper mapper() {
		return new ObjectMapper();
	}
	
	@Bean
	public SimpleTemplateService simpleTemplateService() {
		return new SimpleTemplateService();
	}
	
	@Bean
	public WorkflowConfigurationService workflowConfigurationService() {
		return new WorkflowConfigurationService();
	}
	
	@Bean 
	public WorkflowExecutionService workflowExecutionService() {
		return new WorkflowExecutionService();
	}

	@Bean 
	public WorkflowStatusService workflowStatusService() {
		return new WorkflowStatusService();
	}

	@Bean 
	public WorkflowEngine workflowEngine() {
		return new WorkflowEngine();
	}
	
	@Bean
	public EnvironmentProperties environmentProperties() {
		return new EnvironmentProperties();
	}

	
	@Bean
	public EnrichContextProcessor enrichContextProcessor() {
		return new EnrichContextProcessor();
	}
	
	@Bean
	public SubWorkflowLauncherProcessor subWorkflowLauncherProcessor() {
		return new SubWorkflowLauncherProcessor();
	}
}
