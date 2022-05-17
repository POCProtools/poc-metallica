package fr.insee.metallica.pocprotools.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.insee.metallica.pocprotools.domain.Workflow;
import fr.insee.metallica.pocprotools.domain.WorkflowStep;

public class WorkflowDto {
	private UUID id;
	private Workflow.Status status;
	private String name;
	List<StepDto> step = new ArrayList<>();

	public static class StepDto {
		private UUID id;
		private WorkflowStep.Status status;
		private String label;
		public UUID getId() {
			return id;
		}
		public void setId(UUID id) {
			this.id = id;
		}
		public WorkflowStep.Status getStatus() {
			return status;
		}
		public void setStatus(WorkflowStep.Status status) {
			this.status = status;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Workflow.Status getStatus() {
		return status;
	}

	public void setStatus(Workflow.Status status) {
		this.status = status;
	}

	public List<StepDto> getStep() {
		return step;
	}

	public void setStep(List<StepDto> step) {
		this.step = step;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
