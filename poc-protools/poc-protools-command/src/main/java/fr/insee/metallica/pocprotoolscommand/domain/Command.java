package fr.insee.metallica.pocprotoolscommand.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(indexes = {@Index(columnList = "status"), @Index(columnList = "nextScheduledTime"), @Index(columnList = "limitKey")})
public class Command {
	public static enum Status {
		WaitingToBeScheduled, Pending, Processing, Done, Error, Retry, AwaitingResult
	}
	
	@Id
	@Column(columnDefinition = "UUID")
	private UUID id = UUID.randomUUID();
	
	private String type;
	
	@OneToOne(optional = true)
	private Command resultFetcher;
	
	@OneToOne(fetch = FetchType.LAZY, optional = true, mappedBy = "resultFetcher")
	private Command originalCommand;
	
	@LastModifiedDate
	private LocalDateTime lastUpdate;
	
	private LocalDateTime lastHeartBeat;

	private LocalDateTime nextScheduledTime;

	private int nbTry;
	
	@Enumerated(EnumType.STRING)
	private Status status;

	@Type(type = "text")
	private String context;
	
	@Type(type = "text")
	private String payload;
	
	@Type(type = "text")
	private String result;
	
	private int concurrencyLimit;
	
	private String limitKey;
	
	private Integer rescheduledDelay;
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
	public Command getResultFetcher() {
		return resultFetcher;
	}

	public void setResultFetcher(Command resultFetcher) {
		this.resultFetcher = resultFetcher;
	}
	
	public Command getOriginalCommand() {
		return originalCommand;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getNbTry() {
		return nbTry;
	}

	public void setNbTry(int nbTry) {
		this.nbTry = nbTry;
	}

	public LocalDateTime getNextScheduledTime() {
		return nextScheduledTime;
	}

	public void setNextScheduledTime(LocalDateTime nextScheduledTime) {
		this.nextScheduledTime = nextScheduledTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getLastHeartBeat() {
		return lastHeartBeat;
	}

	public void setLastHeartBeat(LocalDateTime lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}

	public int getConcurrencyLimit() {
		return concurrencyLimit;
	}

	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}

	public String getLimitKey() {
		return limitKey;
	}

	public void setLimitKey(String limitKey) {
		this.limitKey = limitKey;
	}

	public Integer getRescheduledDelay() {
		return rescheduledDelay;
	}

	public void setRescheduledDelay(int rescheduledDelayInSeconds) {
		this.rescheduledDelay = rescheduledDelayInSeconds;
	}
	
	
}
