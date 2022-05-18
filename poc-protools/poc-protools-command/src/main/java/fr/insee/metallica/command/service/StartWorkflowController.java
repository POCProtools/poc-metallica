package fr.insee.metallica.command.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartWorkflowController {
	static Logger log = LoggerFactory.getLogger(StartWorkflowController.class);
	static public class UsernameDto {
		String username;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}
	
	@PostMapping(path = "/start-workflow")
	public CompletableFuture<String> startWorkflow(@RequestBody UsernameDto dto) {
		return null;
	}
	
	@PostMapping(path = "/start-workflow-async")
	public Future<String> startWorkflowAsync(@RequestBody UsernameDto dto) {
		return null;
	}
}
