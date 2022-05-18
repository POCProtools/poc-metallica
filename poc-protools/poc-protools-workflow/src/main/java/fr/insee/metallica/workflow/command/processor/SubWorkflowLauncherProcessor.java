package fr.insee.metallica.workflow.command.processor;

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

public class SubWorkflowLauncherProcessor extends TypedAbstractCommandProcessor<SubWorkflowContext> {
	public final static String Name = "SubWorkflowLauncherProcessor";
	
	public SubWorkflowLauncherProcessor() {
		super(Name, SubWorkflowContext.class);
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
	public UUID process(Command command, SubWorkflowContext payload) throws CommandExecutionException {
		if (command.getResultFetcher() != null) {
			return processCommand(payload);
		} else {
			return fetchResponse(command);
		}
	}

	private UUID fetchResponse(Command command) throws CommandExecutionAbortException, CommandExecutionRetryException {
		try {
			UUID workflowId = UUID.fromString(mapper.readValue(command.getOriginalCommand().getResult(), JsonNode.class).textValue());
			var workflow = workflowRepository.findById(workflowId).orElseThrow(() -> new CommandExecutionAbortException("Cannot find workflow " + workflowId));
			switch(workflow.getStatus()) {
			case Success:
				return workflowId;
			case Error:
				throw new CommandExecutionAbortException("Le workflow " + workflowId + " ended in error");
			case Running:
				throw new CommandExecutionRetryException("The workflow is not over yet");
			default:
				throw new CommandExecutionAbortException("Le workflow " + workflowId + " is not in a known status");
			}
		} catch (JsonProcessingException e) {
			throw new CommandExecutionAbortException("Cannot find workflow " + command.getOriginalCommand().getResult());
		}
	}

	private UUID processCommand(SubWorkflowContext payload) throws CommandExecutionException {
		try {
			var workflow = workflowExecutionService.startWorkflow(payload.getWorkflowName(), payload.getContext());
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
		return commandService.createCommand("SubWorkflowLauncherProcessor")
				.rescheduledDelay(5)
				.payload(command.getPayload())
				.build();
	}
}
