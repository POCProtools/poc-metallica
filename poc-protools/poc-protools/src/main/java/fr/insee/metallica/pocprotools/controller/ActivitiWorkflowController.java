package fr.insee.metallica.pocprotools.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/activity")
public class ActivitiWorkflowController {
	private Logger logger = LoggerFactory.getLogger(ActivitiWorkflowController.class);
	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRuntime taskRuntime;

	@Autowired
	private APITaskConverter apiTaskConverter;

	@Operation(summary = "Start process using processKey")
	@PostMapping(value = "/start-process/{processKey}")
	public String startProcess(@PathVariable String processKey) {
		logger.info("> POST request to start the process: " + processKey);

		var instance = runtimeService.startProcessInstanceByKey(processKey);
		logger.info("Process Instance ID : " + instance);

		return instance.getProcessInstanceId();
	}

	@GetMapping(value = "/processes-diagram/{processDefinitionKey:.*}", produces = "image/svg+xml")
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

	@GetMapping(value = "/processes-diagram/{processInstance:.*}", produces = "image/svg+xml")
	@ResponseBody
	public ResponseEntity<InputStreamResource> processInstanceDiagram(@PathVariable String processInstanceId) {
		var processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		if (processInstance == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		var processDiagramGenerator = new DefaultProcessDiagramGenerator();
		var bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

		var is = processDiagramGenerator.generateDiagram(bpmnModel, List.of(processInstance.getActivityId()));
		return ResponseEntity.ok(new InputStreamResource(is));
	}

	@Operation(summary = "Claim all task by processID")
	@PostMapping("/get-tasks/{processID}")
	public void getTasks(@PathVariable String processID) {
		logger.info(">>> Claim assigned tasks <<<");
		List<org.activiti.engine.task.Task> taskInstances = taskService.createTaskQuery().processInstanceId(processID)
				.active().list();
		if (taskInstances.size() > 0) {
			for (Task t : taskInstances) {
				taskService.addCandidateGroup(t.getId(), "userTeam");
				logger.info("> Claiming task: " + t.getId());
				taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(t.getId()).build());
			}
		} else {
			logger.info("\t \t >> There are no task for me to work on.");
		}

	}

	@GetMapping(value = "/processes/")
	@ResponseBody
	public List<String> getProcesses() {
		return repositoryService.createProcessDefinitionQuery().list()
				.stream().map(ProcessDefinition::getKey).collect(Collectors.toList());
		
	}

	@GetMapping(value = "/processes/{processDefinitionKey:.*}")
	@ResponseBody
	public List<String> getRunningProcesses(@PathVariable String processDefinitionKey) {
		var processInstances = runtimeService.createProcessInstanceQuery().active().list();
		return processInstances.stream().map(ProcessInstance::getId).collect(Collectors.toList());
	}

	@GetMapping(value = "/processes-status/{processInstanceId:.*}")
	@ResponseBody
	public List<org.activiti.api.task.model.Task> getRunningProcessStatus(@PathVariable String processInstanceId) {
		var tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
		return tasks.stream().map(apiTaskConverter::from).collect(Collectors.toList());
	}

	@Operation(summary = "Complete claimed task by processKey, add variables to process")
	@GetMapping("/complete-task/{processID}")
	public void completeTaskA(@PathVariable String processID, @RequestBody HashMap<String, Object> variables) {
		List<org.activiti.engine.task.Task> taskInstances = taskService.createTaskQuery().processInstanceId(processID)
				.active().list();
		logger.info("> Completing task from process : " + processID);
		logger.info("\t > Variables : " + variables.toString());
		if (taskInstances.size() > 0) {
			for (Task t : taskInstances) {
				taskService.addCandidateGroup(t.getId(), "userTeam");
				logger.info("> Claiming task: " + t.getId());
				taskRuntime
						.complete(TaskPayloadBuilder.complete().withTaskId(t.getId()).withVariables(variables).build());
				;
			}
		} else {
			logger.info("\t \t >> There are no task for me to complete");
		}
	}

}
