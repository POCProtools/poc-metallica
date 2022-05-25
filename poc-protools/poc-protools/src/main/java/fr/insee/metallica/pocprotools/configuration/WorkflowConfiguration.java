package fr.insee.metallica.pocprotools.configuration;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.pocprotools.connector.AbstractStartWorkflow;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.domain.Workflow;
import fr.insee.metallica.workflow.domain.Workflow.Status;
import fr.insee.metallica.workflow.service.WorkflowConfigurationService;
import fr.insee.metallica.workflow.service.WorkflowEngine;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

@Configuration
public class WorkflowConfiguration {
	static Logger log = LoggerFactory.getLogger(WorkflowConfiguration.class);
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
	
	/**
	 * Start listening to done and error on workflow to notify activiti task 
	 */
	@Autowired
	public void subscribe(WorkflowEngine engine, RuntimeService runtimeService, ObjectMapper mapper) {
		engine.subscribe((workflow, step, message) -> {
			try {
				var execution = getExecution(runtimeService, mapper, workflow);
				if (execution == null) return;
				
				runtimeService.signalEventReceived("Error", execution.getId(), Map.of("error", message));
			} catch (JsonProcessingException e) {
				log.error("Cannot deserialize context", e);
			} catch (Exception e) {
				log.error("Problem during send event", e);
			}
		}, Status.Error);
		
		engine.subscribe((workflow, step, result) -> {
			try {
				var map = new HashMap<String,Object>();
				if (result != null) {
					var objectNode = mapper.readValue(result, JsonNode.class);
					map.put("workflowResult", objectNode);
				} else {
					map.put("workflowResult", null);
				}
				var execution = getExecution(runtimeService, mapper, workflow);
				if (execution == null) return;
				
				runtimeService.trigger(execution.getId(), map);
			} catch (JsonProcessingException e) {
				log.error("Cannot deserialize context", e);
			} catch (Exception e) {
				log.error("Problem during send event", e);
			}
		}, Status.Success);
	}

	private Execution getExecution(RuntimeService runtimeService, ObjectMapper mapper, Workflow workflow)
			throws JsonProcessingException, JsonMappingException {
		var context = mapper.readValue(workflow.getContext(), ObjectNode.class);
		if (context.get("processInstanceId") == null || !context.get("processInstanceId").isTextual()) {
			log.error("Cannot find processId in workflow context");
			return null;
		}
		if (context.get("activityId") == null || !context.get("activityId").isTextual()) {
			log.error("Cannot find activityId in workflow context");
			return null;
		}
		var processId = context.get("processInstanceId").asText();
		var activityId = context.get("activityId").asText();
		
		var execution = runtimeService.createExecutionQuery()
				.activityId(activityId).processInstanceId(processId).singleResult();
		if (execution == null) {
			log.error("Cannot retrieve execution {} : {}", processId, activityId);
			return null;
		}
		return execution;
	}
}

