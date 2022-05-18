package fr.insee.metallica.pocprotools;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import fr.insee.metallica.pocprotools.controller.StartWorkflowController.UsernameDto;
import fr.insee.metallica.workflow.domain.Workflow;

@FeignClient(value = "protools", url = "${command.services.protools}")
public interface ProtoolsClient {
	@PostMapping(path = "/start-workflow")
	public String startWorkflow(@RequestBody UsernameDto dto);

	@PostMapping(path = "/start-workflow-async")
	public Workflow startWorkflowAsync(@RequestBody UsernameDto dto);
}
