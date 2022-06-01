package fr.insee.metallica.pocprotools;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.activiti.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionAbortException;
import fr.insee.metallica.command.service.CommandProcessorService;
import fr.insee.metallica.pocprotools.connector.AbstractStartWorkflow;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.service.WorkflowConfigurationService;
import fr.insee.metallica.workflow.service.WorkflowExecutionService;

@SpringBootTest
class ActivityTests {
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private WorkflowConfigurationService workflowConfigurationService;
	
	@Autowired
	private WorkflowExecutionService workflowExecutionService;
	
	@Autowired
	private CommandProcessorService commandProcessorService;
	
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private IntegrationTestHelper helper;
	
	static boolean initDone = false;
	static Supplier<?> TestNominal;
	static Function<String, ?> TestSubNominal;
	
	@BeforeEach
	void init() {
		if (initDone)
			return;
		initDone = true;
		workflowConfigurationService.addWorkflow("TestNominal" ,WorkflowDescriptor.Builder()
				.id(UUID.fromString("000000-644b-47dc-b803-074f08f01e2a"))
				.name("TestNominal")
				.addStep()
					.label("do nothing")
					.id(UUID.fromString("000000-5383-4cda-8881-46c1fff7443a"))
					.type("TestNominal")
					.payloadTemplate("echo")
					.initialStep()
					.finalStep()
					.build());
		
		workflowConfigurationService.addWorkflow("TestSubNominal" ,WorkflowDescriptor.Builder()
				.id(UUID.fromString("000006-644b-47dc-b803-074f08f01e2a"))
				.name("TestSubNominal")
				.addStep()
					.label("do nothing")
					.id(UUID.fromString("000006-5383-4cda-8881-46c1fff7443a"))
					.type("TestSubNominal")
					.payloadTemplate("{\"ue\": \"${context.ue}\"}")
					.initialStep()
					.finalStep()
					.build());
		
		beanFactory.registerSingleton("TestNominal", new AbstractStartWorkflow(workflowExecutionService) {
			@Override
			public WorkflowDescriptor getDescriptor() {
				return workflowConfigurationService.getWorkflow("TestNominal");
			}
		});
		
		beanFactory.registerSingleton("TestSubNominal", new AbstractStartWorkflow(workflowExecutionService) {
			@Override
			public WorkflowDescriptor getDescriptor() {
				return workflowConfigurationService.getWorkflow("TestSubNominal");
			}
		});
		
		commandProcessorService.registerProcessor("TestNominal", (Command command) -> {
			return TestNominal.get();
		});
		
		commandProcessorService.registerProcessor("TestSubNominal", (Command command) -> {
			try {
				var ue = mapper.readValue(command.getPayload(), ObjectNode.class).get("ue").asText();
				return TestSubNominal.apply(ue);
			} catch (JsonProcessingException e) {
				throw new CommandExecutionAbortException("Cannot deserialize the ue");
			}
		});
	}
	
	@Test
	void testNominal() throws Throwable {
		var b = new AtomicBoolean(false);
		TestNominal = () -> {
			b.set(true);
			return Map.of("result", "good");
		};

		var instance = runtimeService.startProcessInstanceByKey("MainTest");
		System.out.println(instance.getProcessInstanceId());
		Thread.sleep(1000);
		
		assert b.get();
		assert runtimeService.createExecutionQuery().processInstanceId(instance.getId()).list().isEmpty();
		
	}

	@Test
	void testErreur() throws Throwable {
		var b = new AtomicBoolean(false);
		TestNominal = () -> {
			b.set(true);
			throw new RuntimeException(); 
		};

		var instance = runtimeService.startProcessInstanceByKey("MainTest");
		System.out.println(instance.getProcessInstanceId());
		Thread.sleep(1000);
		
		assert b.get();
		assert runtimeService.createExecutionQuery().processInstanceId(instance.getId()).list().isEmpty();
		
	}

	@Test
	void testMulti() throws Throwable {
		var b = new AtomicBoolean(false);
		TestNominal = () -> {
			b.set(true);
			return List.of("Henry Des", "Anne Sylvestre", "Joey Starr");
		};
		var i = new AtomicInteger(0);
		var list = new ConcurrentLinkedQueue<>();
		TestSubNominal = (ue) -> {
			i.incrementAndGet();
			list.add(ue);
			return Map.of("success", ue);
		};

		var instance = runtimeService.startProcessInstanceByKey("TestSubWorkflow");
		System.out.println(instance.getProcessInstanceId());
		
		helper.waitFor(3, () -> runtimeService.createExecutionQuery().processInstanceId(instance.getId()).list().isEmpty());
		assert b.get();
		assert i.get() == 3;
		assert runtimeService.createExecutionQuery().processInstanceId(instance.getId()).list().isEmpty();
		
		assert list.contains("Henry Des");
		assert list.contains("Anne Sylvestre");
		assert list.contains("Joey Starr");
		
	}
}
