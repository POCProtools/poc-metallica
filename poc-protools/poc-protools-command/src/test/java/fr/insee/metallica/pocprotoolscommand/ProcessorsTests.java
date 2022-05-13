package fr.insee.metallica.pocprotoolscommand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.mock.MockApplication;
import fr.insee.metallica.mock.MockTestController;
import fr.insee.metallica.pocprotoolscommand.domain.Command.Status;
import fr.insee.metallica.pocprotoolscommand.processor.Processors;
import fr.insee.metallica.pocprotoolscommand.processor.payload.HttpPayload.HttpMethod;
import fr.insee.metallica.pocprotoolscommand.processor.payload.ServiceHttpPayload;
import fr.insee.metallica.pocprotoolscommand.processor.payload.UrlHttpPayload;
import fr.insee.metallica.pocprotoolscommand.repository.CommandRepository;
import fr.insee.metallica.pocprotoolscommand.service.CommandService;

@SpringBootTest(classes = MockApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class ProcessorsTests {
	@Autowired
	private CommandService commandService;	
	
	@Autowired
	private CommandRepository commandRepository;
	
	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testHttp() throws Throwable {
		var payload = new UrlHttpPayload();
		payload.setBody(new MockTestController.TestBody("hey "));
		payload.setMethod(HttpMethod.POST);
		payload.setUrl("http://localhost:18181/test-post");
		
		var command = commandService.createCommand(Processors.Http)
							.payload(payload)
							.saveAndSend();
		for(var i = 0; i < 10; i++) {
			Thread.sleep(100);
			var dbCommand = commandRepository.findById(command.getId()).orElse(null);
			if (dbCommand.getStatus() == Status.Done) break;
		}
		var dbCommand = commandRepository.findById(command.getId()).orElse(null);
		assert dbCommand.getStatus() == Status.Done;
		
		var result = mapper.readValue(dbCommand.getResult(), MockTestController.TestBody.class);
		assert result.value.equals("hey done");
	}

	@Test
	public void testHttpService() throws Throwable {
		var payload = new ServiceHttpPayload();
		payload.setBody(new MockTestController.TestBody("hey "));
		payload.setMethod(HttpMethod.POST);
		payload.setPath("/test-post");
		payload.setService("servicename");
		
		var command = commandService.createCommand(Processors.ServiceHttp)
							.payload(payload)
							.saveAndSend();
		for(var i = 0; i < 10; i++) {
			Thread.sleep(100);
			var dbCommand = commandRepository.findById(command.getId()).orElse(null);
			if (dbCommand.getStatus() == Status.Done) break;
		}
		var dbCommand = commandRepository.findById(command.getId()).orElse(null);
		assert dbCommand.getStatus() == Status.Done;
		
		var result = mapper.readValue(dbCommand.getResult(), MockTestController.TestBody.class);
		assert result.value.equals("hey done");
	}
}
