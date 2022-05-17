package fr.insee.metallica.pocprotools.controller;

import java.util.UUID;
import java.util.concurrent.Future;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.metallica.pocprotools.domain.Workflow;
import fr.insee.metallica.pocprotools.service.WorkflowEngine;
import fr.insee.metallica.pocprotools.service.WorkflowService;

@RestController
public class StartWorkflowController {
	static Logger log = LoggerFactory.getLogger(StartWorkflowController.class);
	static public class UsernameDto {
		@NotBlank
		String username;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}
	
	@Autowired
	private WorkflowEngine workflowEngine;
	
	@Autowired
	private WorkflowService workflowService;
	
	@PostMapping(path = "/start-workflow")
	public DeferredResult<ResponseEntity<String>> startWorkflow(@Valid @RequestBody UsernameDto dto) {
		var result = new DeferredResult<ResponseEntity<String>>();
		var future = workflowEngine.startWorkflowAndWait("GeneratePasswordAndSendMail", dto);
		future.whenComplete((res, ex) -> 
			result.setResult(
				ex != null ?
				ResponseEntity.internalServerError().body(ex.getMessage()) :
				ResponseEntity.ok("Message was sent")
			)
		);
		return result;
	}
	
	@PostMapping(path = "/start-workflow-async-mail")
	public DeferredResult<ResponseEntity<String>> startWorkflowAsyncMail(@Valid @RequestBody UsernameDto dto) {
		var result = new DeferredResult<ResponseEntity<String>>();
		var future = workflowEngine.startWorkflowAndWait("GeneratePasswordAndSendMailWithAsyncResult", dto);
		future.whenComplete((res, ex) -> 
			result.setResult(
				ex != null ?
				ResponseEntity.internalServerError().body(ex.getMessage()) :
				ResponseEntity.ok("Message was sent")
			)
		);
		return result;
	}
	
	@PostMapping(path = "/start-workflow-async")
	public Future<Workflow> startWorkflowAsync(@Valid @RequestBody UsernameDto dto) {
		try {
			return workflowEngine.startWorkflow("GeneratePasswordAndSendMail", dto);
		} catch (JsonProcessingException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not serialize the Dto");
		}
	}

	@GetMapping(path = "/workflow/{id}")
	@ResponseBody
	public WorkflowDto getStatus(@PathVariable("id") UUID id) {
		return workflowService.getStatus(id);
	}
}
