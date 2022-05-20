package fr.insee.metallica.pocprotools.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.task.model.Task;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity/process-instance")
public class ActivitiProcessInstanceController {
	protected Logger logger = LoggerFactory.getLogger(ActivitiProcessInstanceController.class);
	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private APITaskConverter apiTaskConverter;

	@GetMapping(value = "/")
	@ResponseBody
	public List<String> getProcessesInstance() {
		return runtimeService.createProcessInstanceQuery().list()
			.stream().map(ProcessInstance::getId).collect(Collectors.toList());
	}

	@GetMapping(value = "/by-process-definition/{processDefinitionKey:.*}")
	@ResponseBody
	public List<String> getRunningProcesses(@PathVariable String processDefinitionKey) {
		var processInstances = runtimeService.createProcessInstanceQuery()
				.processDefinitionKey(processDefinitionKey).list();
		return processInstances.stream().map(ProcessInstance::getId).collect(Collectors.toList());
	}

	@GetMapping(value = "/active")
	@ResponseBody
	public List<String> getActiveProcessesInstance() {
		return runtimeService.createProcessInstanceQuery().active().list()
			.stream().map(ProcessInstance::getId).collect(Collectors.toList());
	}

	@GetMapping(value = "/{processInstanceId:.*}/tasks")
	@ResponseBody
	public List<Task> getProcessInstanceStatus(@PathVariable String processInstanceId) {
		var tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		return tasks.stream().map(apiTaskConverter::from).collect(Collectors.toList());
	}

	@GetMapping(value = "/{processInstanceId:.*}/diagram", produces = "image/svg+xml")
	@ResponseBody
	public ResponseEntity<InputStreamResource> processInstanceDiagram(@PathVariable String processInstanceId) {
		var processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		if (processInstance == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		var processDiagramGenerator = new DefaultProcessDiagramGenerator();
		var bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		
		var tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list().
			stream().map(org.activiti.engine.task.Task::getTaskDefinitionKey).collect(Collectors.toList());


		var is = processDiagramGenerator.generateDiagram(bpmnModel, tasks);
		return ResponseEntity.ok(new InputStreamResource(is));
	}

}
