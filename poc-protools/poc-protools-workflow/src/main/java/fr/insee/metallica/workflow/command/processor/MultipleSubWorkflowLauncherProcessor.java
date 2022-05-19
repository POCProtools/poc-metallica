package fr.insee.metallica.workflow.command.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.exception.CommandExecutionRetryException;
import fr.insee.metallica.command.processor.TypedAbstractCommandProcessor;
import fr.insee.metallica.command.service.CommandService;
import fr.insee.metallica.workflow.repository.WorkflowRepository;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

public class MultipleSubWorkflowLauncherProcessor extends TypedAbstractCommandProcessor<MultipleSubWorkflowContext> {
	public final static String Name = "MultipleSubWorkflow";
	
	public MultipleSubWorkflowLauncherProcessor() {
		super(Name, MultipleSubWorkflowContext.class);
	}
	
	@Autowired
	private WorkflowExecutionService workflowExecutionService;
	
	@Autowired
	private WorkflowRepository workflowRepository;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private ObjectMapper mapper;

	@Override
	@Transactional
	public List<UUID> process(Command command, MultipleSubWorkflowContext payload) throws CommandExecutionException {
		if (command.getResultFetcher() != null) {
			List<UUID> uuids = new ArrayList<>(payload.getContexts().size());
			for (var context : payload.getContexts()) {
				uuids.add(processCommand(payload.getWorkflowName(), context));
			}
				
			return uuids;
		} else {
			return fetchResponse(command);
		}
	}

	private List<UUID> fetchResponse(Command command) throws CommandExecutionAbortException, CommandExecutionRetryException {
		try {
			UUID[] workflowIds = mapper.readValue(command.getOriginalCommand().getResult(), UUID[].class);
			for (var workflowId : workflowIds) {
				var workflow = workflowRepository.findById(workflowId).orElseThrow(() -> new CommandExecutionAbortException("Cannot find workflow " + workflowId));
				switch(workflow.getStatus()) {
				case Success:
					break;
				case Error:
					throw new CommandExecutionAbortException("Le workflow " + workflowId + " ended in error");
				case Running:
					throw new CommandExecutionRetryException("The workflow "+ workflowId + " is not over yet");
				default:
					throw new CommandExecutionAbortException("Le workflow " + workflowId + " is not in a known status");
				}
			}
			return List.of(workflowIds);
		} catch (JsonProcessingException e) {
			throw new CommandExecutionAbortException("Cannot find workflow " + command.getOriginalCommand().getResult());
		}
	}

	private UUID processCommand(String workflowName, JsonNode context) throws CommandExecutionException {
		try {
			var workflow = workflowExecutionService.startWorkflow(workflowName, context);
			return workflow.getId();
		} catch (JsonProcessingException e) {
			throw new CommandExecutionException("could not start workflow: " + e.getMessage(), e);
		}
	}
	
	@Override
	public boolean isAsynchronousResult() {
		return true;
	}
	
	@Override
	public Command getAsyncResultCommand(Command command) {
		return commandService.createCommand(Name)
				.rescheduledDelay(5)
				.payload(command.getPayload())
				.build();
	}
}
