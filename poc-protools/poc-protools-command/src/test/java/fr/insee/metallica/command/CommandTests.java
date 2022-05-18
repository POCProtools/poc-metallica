package fr.insee.metallica.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.command.domain.Command.Status;
import fr.insee.metallica.command.exception.CommandExecutionRetryException;
import fr.insee.metallica.command.repository.CommandRepository;
import fr.insee.metallica.command.service.CommandEngine;
import fr.insee.metallica.command.service.CommandScheduler;
import fr.insee.metallica.command.service.CommandService;
import fr.insee.metallica.command.service.CommandEventListener.Type;
import fr.insee.metallica.mock.MockApplication;

@SpringBootTest(classes = MockApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
class CommandTests {
	@Autowired
	private CommandService commandService;	
	
	@Autowired
	private CommandEngine commandEngine;	
	
	@Autowired
	private CommandScheduler commandScheduler;	
	
	@Autowired
	private CommandRepository commandRepository;
	
	@Autowired
	private ObjectMapper mapper;

	@Test
	void testNominal() throws Throwable {
		var command = commandService.createCommand("Print")
							.payload(Map.of("username", "jean"))
							.saveNoSend();
		var dbCommand = commandRepository.findById(command.getId()).orElse(null);
		assert dbCommand != null;
		assert dbCommand.getId().equals(command.getId());
		assert dbCommand.getStatus() == Status.Pending;
		var context = mapper.readValue(dbCommand.getPayload(), ObjectNode.class);
		assert context.get("username") != null;
		assert context.get("username").asText().equals("jean");
		
		var id = command.getId();
		
		var commandToProcess = commandService.aquireToProcess(id);
		assert commandToProcess != null;
		assert commandToProcess.getId().equals(id);
		assert commandToProcess.getStatus() == Status.Processing;
		
		dbCommand = commandRepository.findById(command.getId()).orElseThrow();
		assert dbCommand.getStatus() == Status.Processing;
		
		assert commandService.aquireOneToProcess() == null;
		
		commandService.done(command, Map.of("password", "Maisoui!!!"));
		
		dbCommand = commandRepository.findById(command.getId()).orElseThrow();
		assert dbCommand.getStatus() == Status.Done;
		assert dbCommand.getResult() != null;
		
		var result = mapper.readValue(dbCommand.getResult(), ObjectNode.class);
		assert result.get("password") != null;
		assert result.get("password").asText().equals("Maisoui!!!");
	}

	@Test
	void testNominalAsync() throws Throwable {
		var lock = new Object();
		commandEngine.registerProcessor("async-create-file", (command) -> {
			new Thread(() -> {
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
					}
				}
				var file = new File(command.getPayload());
				try (var stream = new PrintStream(new FileOutputStream(file))) {
					stream.println("done");
				} catch (FileNotFoundException e) {
				}
			}).start();
			return "started";
		});
		
		var compteur = new AtomicInteger(0);
		var rescheduleCompteur = new AtomicInteger(0);
		commandEngine.registerProcessor("check-file", (command) -> {
			var file = new File(command.getPayload());
			compteur.incrementAndGet();
			if (!file.exists()) {
				rescheduleCompteur.incrementAndGet();
				throw new CommandExecutionRetryException("file not yet available");
			}
			return "file exists";
		});
		var file = File.createTempFile("testfile", ".txt");
		file.delete();
		var filePath = file.getAbsolutePath();
		
		var command = commandService.createCommand("async-create-file")
							.payload(filePath)
							.asyncResult("check-file")
							.rescheduledDelay(1)
							.payload(filePath)
							.saveAndSend();
		
		var commandId = command.getId();
		
		var dbCommand = commandRepository.findById(commandId).orElse(null);
		assert dbCommand != null;
		assert dbCommand.getId().equals(command.getId());
		assert dbCommand.getResultFetcher() != null;
		
		var fecherId = dbCommand.getResultFetcher().getId();
		
		waitFor(1, () -> commandRepository.findById(commandId).orElse(null).getStatus() == Status.AwaitingResult);
		waitFor(10, () -> compteur.get() > 0);
		
		assert compteur.get() > 0;
		assert rescheduleCompteur.get() > 0;
		
		dbCommand = commandRepository.findById(command.getId()).orElse(null);
		
		assert dbCommand.getStatus() == Status.AwaitingResult;
		assert dbCommand.getResultFetcher().getStatus() == Status.Processing || 
			   dbCommand.getResultFetcher().getStatus() == Status.Retry;
		
		synchronized (lock) {
			lock.notify();
		}
		waitFor(10, () -> commandRepository.findById(fecherId).orElse(null).getStatus() == Status.Done);
		
		dbCommand = commandRepository.findById(command.getId()).orElse(null);
		assert dbCommand.getStatus() == Status.Done;
		assert dbCommand.getResult().equals("file exists");
	}

	@Test
	void resurection() throws Throwable {
		AtomicInteger b = new AtomicInteger();
		commandEngine.registerProcessor("test-resurection", (command) -> {
			return b.incrementAndGet();
		});
		
		var command = commandService.createCommand("test-resurection")
							.saveNoSend();
		
		commandRepository.setStatus(command.getId(), Status.Pending, Status.Processing);
		commandRepository.heartBeat(command.getId(), LocalDateTime.now().minusSeconds(10));
		// the command should to be considered dead
		commandScheduler.checkForDeadCommand();
		
		assert waitFor(10, () -> commandRepository.findById(command.getId()).orElseThrow().getStatus() == Status.Done);
	}

	@Test
	void retry() throws Throwable {
		AtomicInteger b = new AtomicInteger();
		commandEngine.registerProcessor("test-retry", (command) -> {
			return b.incrementAndGet();
		});
		
		var command = commandService.createCommand("test-retry")
				.scheduledTime(LocalDateTime.now().plusSeconds(3))
				.saveNoSend();
		
		var commandId = command.getId();
		
		commandScheduler.checkForScheduledCommand();
		assert commandRepository.findById(commandId).orElseThrow().getStatus() == Status.Pending;
		assert waitFor(10, () -> {
			commandScheduler.checkForScheduledCommand();
			return commandRepository.findById(commandId).orElseThrow().getStatus() == Status.Done;
		});
	}

	@Test
	void testConcurrent() throws Throwable {
		Set<String> result = new HashSet<String>();
		AtomicInteger b = new AtomicInteger(0);
		AtomicInteger c = new AtomicInteger(0);
		
		commandEngine.registerProcessor("test", (command) -> {
			return b.incrementAndGet();
		});
		int nbCommand = 5000;
		
		commandService.subscribe((command, body) -> {
			System.out.println(c.incrementAndGet());
			result.add(command.getPayload());
		}, Type.Done, Type.Error);
		
		for (int i = 0; i < nbCommand; i++) {
			commandService.createCommand("test")
				.payload(Map.of("username", "jean" + i))
				.saveAndSend();
		}
		assert waitFor(100, () -> {
			System.out.println(result.size());
			System.out.println(b.get());
			return result.size() >= nbCommand;
		});
		assert result.size() == nbCommand;
		assert b.get() == nbCommand;
	}

	@Test
	void testConcurrentLimit() throws Throwable {
		Set<String> result = new HashSet<String>();
		AtomicInteger b = new AtomicInteger(0);
		AtomicInteger c = new AtomicInteger(0);
		
		AtomicInteger concurrent = new AtomicInteger(0);
		AtomicInteger maxConcurrent = new AtomicInteger(0);
		
		commandEngine.registerProcessor("test-concurrent-limit", (command) -> {
			var currentConcurrency = concurrent.incrementAndGet();
			if (currentConcurrency > maxConcurrent.get()) {
				maxConcurrent.set(currentConcurrency);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			concurrent.decrementAndGet();
			return b.incrementAndGet();
		});
		int nbCommand = 5;
		
		commandService.subscribe((command, body) -> {
			System.out.println(c.incrementAndGet());
			result.add(command.getPayload());
		}, Type.Done, Type.Error);
		
		String limitKey = "testLimit-" + new Random().nextLong();
		
		for (int i = 0; i < nbCommand; i++) {
			commandService.createCommand("test-concurrent-limit")
				.payload(Map.of("username", "jean" + i))
				.saveAndSend(2, limitKey);
		}
		assert waitFor(100, () -> {
			System.out.println(result.size());
			System.out.println(b.get());
			return result.size() >= nbCommand;
		});
		assert result.size() == nbCommand;
		assert b.get() == nbCommand;
		assert maxConcurrent.get() == 2;
	}
	
	private boolean waitFor(int timeMax, Supplier<Boolean> b) {
		var startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() < 1000 * timeMax + startTime) {
			if (b.get()) return true;
			sleep(100);
		}
		return b.get();
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}
}
