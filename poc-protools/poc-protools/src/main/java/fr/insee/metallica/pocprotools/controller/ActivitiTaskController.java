package fr.insee.metallica.pocprotools.controller;

import java.util.List;
import java.util.Map;

import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/activity/task")
public class ActivitiTaskController {
	protected Logger logger = LoggerFactory.getLogger(ActivitiTaskController.class);

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRuntime taskRuntime;

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

	@Operation(summary = "Complete claimed task by processKey, add variables to process")
	@PostMapping("/complete-task/{processID}")
	public void completeTask(@PathVariable String processID, @RequestBody Map<String, Object> variables) {
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
