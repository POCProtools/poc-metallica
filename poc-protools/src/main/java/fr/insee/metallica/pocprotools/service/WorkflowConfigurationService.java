package fr.insee.metallica.pocprotools.service;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.insee.metallica.pocprotools.configuration.WorkflowsProperties;
import fr.insee.metallica.pocprotools.controller.WorkflowDescriptor;

@Service
public class WorkflowConfigurationService {
	private Map<String, WorkflowDescriptor> workflows;
	
	@PostConstruct
	public void initialize() throws IOException {
		var mapper = new ObjectMapper(new YAMLFactory());
		try(var stream = getClass().getResourceAsStream("/workflows.yaml")) {
			var workflows = mapper .readValue(stream, WorkflowsProperties.class);
			this.workflows = workflows.toWorkflowDescriptors();
		}
	}
	
	public WorkflowDescriptor getWorkflow(String name) {
		return workflows.get(name);
	}
}
