package fr.insee.metallica.workflow.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.insee.metallica.workflow.configuration.WorkflowsProperties;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;

public class WorkflowConfigurationService {
	private Map<String, WorkflowDescriptor> workflows;
	private Map<UUID, WorkflowDescriptor> workflowsByUuid;
	
	@PostConstruct
	public void initialize() throws IOException {
		var mapper = new ObjectMapper(new YAMLFactory());
		try(var stream = getClass().getResourceAsStream("/workflows.yaml")) {
			var workflows = mapper.readValue(stream, WorkflowsProperties.class);
			this.workflows = workflows.toWorkflowDescriptors();
			this.workflowsByUuid = this.workflows.values().stream().collect(Collectors.toMap(WorkflowDescriptor::getId, w -> w));
		}
	}
	
	public WorkflowDescriptor getWorkflow(String name) {
		return workflows.get(name);
	}
	
	public WorkflowDescriptor getWorkflow(UUID uuid) {
		return workflowsByUuid.get(uuid);
	}
	
	public void addWorkflow(String key, WorkflowDescriptor desc) {
		workflows.put(key, desc);
		workflowsByUuid.put(desc.getId(), desc);
	}
}
