package fr.insee.metallica.pocprotools.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class WorkflowStep {
	public static enum Status {
		Pending,
		Running,
		Error,
		Retry,
		Success
	}
	
	@Id
	@Column(columnDefinition = "UUID")
	private UUID id = UUID.randomUUID();
	
	@ManyToOne
	private Workflow workflow;
	
	@Column(columnDefinition = "UUID")
	private UUID stepId;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Version
	private int version;
	
	@Lob
	private String context;

	public UUID getId() {
		return id;
	}

	public UUID getStepId() {
		return stepId;
	}

	public void setStepId(UUID stepId) {
		this.stepId = stepId;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getVersion() {
		return version;
	}
}
