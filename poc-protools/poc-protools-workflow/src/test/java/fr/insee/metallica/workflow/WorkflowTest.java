package fr.insee.metallica.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.metallica.command.service.CommandProcessorService;
import fr.insee.metallica.mock.MockProtoolsApplication;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

@SpringBootTest(classes = MockProtoolsApplication.class)
public class WorkflowTest {
	public static class TestDto {
		public String value;
	}
	
	@Autowired
	private WorkflowExecutionService workflowExecutionService;
	
	@Autowired
	private CommandProcessorService commandProcessorService;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testParse() throws JsonProcessingException {
		List<String> result = new ArrayList<>(); 
		commandProcessorService.registerProcessor("TestNominalProcessor", command -> result.add(command.getPayload()));
		
		commandProcessorService.registerProcessor("TestNominalProcessorObject", command -> { 
			result.add(readValue(command.getPayload(), TestDto.class).value);
			return "valeur de previous result";
		});
		
		workflowExecutionService.executeWorkflow("TestNominal", Map.of("contextValue", "good"))
			.join();
		
		assert result.get(0).equals("good") ;
		assert result.get(1).equals("ca marche !") ;
		assert result.get(2).equals("valeur de context");
		assert result.get(3).equals("valeur de previous result");

	}

	@Test
	public void testSubworkflow() throws JsonProcessingException {
		List<String> result = new ArrayList<>(); 
		commandProcessorService.registerProcessor("TestSubworkflowProcessor", command -> result.add(command.getPayload()));
		
		workflowExecutionService.executeWorkflow("TestSubworkflow", Map.of())
			.join();
		
		assert result.get(0).equals("testSubWorkflow") ;
	}

	
	private <T> T readValue(String payload, Class<T> clazz) {
		try {
			return mapper.readValue(payload, clazz);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
