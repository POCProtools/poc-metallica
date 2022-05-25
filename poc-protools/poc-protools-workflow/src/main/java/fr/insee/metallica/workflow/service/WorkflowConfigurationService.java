package fr.insee.metallica.workflow.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.insee.metallica.workflow.configuration.WorkflowsProperties;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;

public class WorkflowConfigurationService {
	private Map<String, WorkflowDescriptor> workflows;
	private Map<UUID, WorkflowDescriptor> workflowsByUuid;
	
	@Value("classpath:workflows-*.yaml")
	private Resource[] workflowsYaml;
	
	@PostConstruct
	public void initialize() throws IOException {
		var mapper = new ObjectMapper(new YAMLFactory());
		try(var stream = getClass().getResourceAsStream("/workflows.yaml")) {
			if (stream != null) {
				var workflows = mapper.readValue(stream, WorkflowsProperties.class);
				this.workflows = workflows.toWorkflowDescriptors();
			}
		}
		for (var resource : workflowsYaml) {
			try (var stream = resource.getInputStream()) {
				var workflows = mapper.readValue(resource.getInputStream(), WorkflowsProperties.class);
				this.workflows.putAll(workflows.toWorkflowDescriptors());
			}
		}
		this.workflowsByUuid = this.workflows.values().stream().collect(Collectors.toMap(WorkflowDescriptor::getId, w -> w));
	}
	
	public WorkflowDescriptor getWorkflow(String name) {
		return workflows.get(name);
	}
	
	public WorkflowDescriptor getWorkflow(UUID uuid) {
		return workflowsByUuid.get(uuid);
	}

	public Collection<WorkflowDescriptor> getWorkflows() {
		return workflowsByUuid.values();
	}
	
	public void addWorkflow(String key, WorkflowDescriptor desc) {
		workflows.put(key, desc);
		workflowsByUuid.put(desc.getId(), desc);
	}
}
