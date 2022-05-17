package fr.insee.metallica.pocprotoolscommand.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

import fr.insee.metallica.pocprotoolscommand.configuration.CommandProperties;
import fr.insee.metallica.pocprotoolscommand.domain.Command.Status;
import fr.insee.metallica.pocprotoolscommand.repository.CommandRepository;

public class CommandScheduler {
	static final Logger log = LoggerFactory.getLogger(CommandScheduler.class);
	
	@Autowired
	private CommandProperties commandProperties;
	
	@Autowired
	private CommandService commandService;

	@Autowired
	private CommandEngine commandEngine;

	@Autowired
	private CommandRepository commandRepository;


	@Scheduled(fixedDelayString = "${command.schedule.delayHeartBeat:5}", timeUnit = TimeUnit.SECONDS)
	public void updateOwnedCommand() {
		List<UUID> currentCommands;
		synchronized (commandEngine.getCurrentCommands()) {
			currentCommands = List.copyOf(commandEngine.getCurrentCommands());
		}
		for (var id : currentCommands) {
			commandService.heartBeat(id);
		}
	}
	
	@Scheduled(fixedDelayString = "${command.schedule.delayBeetweenRetryCheck:5}", timeUnit = TimeUnit.SECONDS)
	public void checkForScheduledCommand() {
		for (var command : commandRepository.findCommandsScheduled(100)) {
			commandEngine.startProcess(command.getId());
		}
	}
	
	@Scheduled(fixedDelayString = "${command.schedule.delayBeetweenDeadCheck:5}", timeUnit = TimeUnit.SECONDS)
	public void checkForDeadCommand() {
		for(int i = 0; i < 100; i++) {
			var page = commandRepository.findPageByStatusAndLastHeartBeatLessThanEqual(Status.Processing, LocalDateTime.now().minusSeconds(commandProperties.getSchedule().getTimeWithoutHeartBeatBeforeDeath()), PageRequest.of(0, 10));
			if (page.getNumberOfElements() == 0) return;
			
			page.stream().forEach((c) -> { 
				if (commandRepository.setStatus(c.getId(), Status.Processing, Status.Pending) > 0)
					commandEngine.startProcess(c.getId());
			});
		}
	}
}
