package fr.insee.metallica.workflow.service;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.workflow.domain.WorkflowStep;
import fr.insee.metallica.workflow.domain.WorkflowStep.Status;
import fr.insee.metallica.workflow.dto.WorkflowStatusDto;
import fr.insee.metallica.workflow.dto.WorkflowStatusDto.StepDto;
import fr.insee.metallica.workflow.repository.WorkflowRepository;
import fr.insee.metallica.workflow.repository.WorkflowStepRepository;

public class WorkflowStatusService {
	@Autowired
	private WorkflowRepository workflowRepository;
	
	@Autowired
	private WorkflowConfigurationService workflowConfigurationService;
	
	@Autowired
	private WorkflowStepRepository workflowStepRepository;
	
	@Transactional
	public WorkflowStatusDto getStatus(UUID id) {
		var workflow = workflowRepository.getById(id);
		var desc = workflowConfigurationService.getWorkflow(workflow.getWorkflowId());
		
		var dto = new WorkflowStatusDto();
		dto.setId(workflow.getId());
		dto.setStatus(workflow.getStatus());
		dto.setName(desc.getName());
		
		var steps = workflowStepRepository.findByWorkflowId(id).stream()
				.collect(Collectors.toMap(WorkflowStep::getStepId, s -> s));
		var stepDesc = desc.getInitialStep();
		
		while(stepDesc != null) {
			
			var stepDto = new StepDto();
			stepDto.setLabel(stepDesc.getLabel());
			
			var step = steps.get(stepDesc.getId());
			if (step != null) {
				stepDto.setId(step.getId());
				stepDto.setStatus(step.getStatus());
			} else {
				stepDto.setStatus(Status.Waiting);
			}
			dto.getStep().add(stepDto);
			stepDesc = stepDesc.getNextStep();
		}
		return dto;
	}
}
