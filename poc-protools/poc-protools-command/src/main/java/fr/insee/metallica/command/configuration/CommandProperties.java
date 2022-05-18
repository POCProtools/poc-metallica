package fr.insee.metallica.command.configuration;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "command")
public class CommandProperties {
	public static class Schedule {
	    int delayHeartBeat = 4;
	    int delayBeetweenRetryCheck = 3;
	    int delayBeetweenDeadCheck = 5;
	    int timeWithoutHeartBeatBeforeDeath = 20;

		public int getDelayHeartBeat() {
			return delayHeartBeat;
		}
		public void setDelayHeartBeat(int delayHeartBeat) {
			this.delayHeartBeat = delayHeartBeat;
		}
		public int getDelayBeetweenRetryCheck() {
			return delayBeetweenRetryCheck;
		}
		public void setDelayBeetweenRetryCheck(int delayBeetweenRetryCheck) {
			this.delayBeetweenRetryCheck = delayBeetweenRetryCheck;
		}
		public int getDelayBeetweenDeadCheck() {
			return delayBeetweenDeadCheck;
		}
		public void setDelayBeetweenDeadCheck(int delayBeetweenDeadCheck) {
			this.delayBeetweenDeadCheck = delayBeetweenDeadCheck;
		}
		public int getTimeWithoutHeartBeatBeforeDeath() {
			return timeWithoutHeartBeatBeforeDeath;
		}
		public void setTimeWithoutHeartBeatBeforeDeath(int timeWithoutHeartBeatBeforeDeath) {
			this.timeWithoutHeartBeatBeforeDeath = timeWithoutHeartBeatBeforeDeath;
		}
	}

	private Schedule schedule = new Schedule();
	
	private Map<String, String> services;

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Map<String, String> getServices() {
		return services;
	}

	public void setServices(Map<String, String> services) {
		this.services = services;
	}
}
