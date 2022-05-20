package fr.insee.metallica.pocprotools.controller;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/activity/process-definition")
public class ActivitiProcessDefinitionController {
	protected Logger logger = LoggerFactory.getLogger(ActivitiProcessDefinitionController.class);
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private RepositoryService repositoryService;


	@Operation(summary = "Start process using processKey")
	@PostMapping(value = "/{processDefinitionKey}/start")
	public String startProcess(@PathVariable String processDefinitionKey) {
		logger.info("> POST request to start the process: " + processDefinitionKey);

		var instance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
		logger.info("Process Instance ID : " + instance);

		return instance.getProcessInstanceId();
	}


	@GetMapping(value = "/{processDefinitionKey}/diagram", produces = "image/svg+xml")
	@ResponseBody
	public ResponseEntity<InputStreamResource> processDefinitionDiagram(@PathVariable String processDefinitionKey) {
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
		if (processDefinition == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		var processDiagramGenerator = new DefaultProcessDiagramGenerator();
		var bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

		InputStream is = processDiagramGenerator.generateDiagram(bpmnModel, List.of());
		return ResponseEntity.ok(new InputStreamResource(is));
	}

	@GetMapping(value = "/{processDefinitionKey}")
	@ResponseBody
	public ProcessDefinition getProcessDefinition(@PathVariable String processDefinitionKey) {
		return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
				.latestVersion().singleResult();
		
	}

	@GetMapping(value = "/")
	@ResponseBody
	public List<String> listProcessDefinition() {
		return repositoryService.createProcessDefinitionQuery().list()
				.stream().map(ProcessDefinition::getKey).collect(Collectors.toList());
	}
}
