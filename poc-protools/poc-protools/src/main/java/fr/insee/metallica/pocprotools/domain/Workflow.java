package fr.insee.metallica.pocprotools.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity
public class Workflow {
	public enum Status {
		Running, Success, Error
	}
	
	@Id
	@Column(columnDefinition = "UUID")
	private UUID id = UUID.randomUUID();
	
	private UUID workflowId;
	
	@Lob
	private String context;

	@Version
	private int version;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	public int getVersion() {
		return version;
	}

	public UUID getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(UUID workflowId) {
		this.workflowId = workflowId;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
