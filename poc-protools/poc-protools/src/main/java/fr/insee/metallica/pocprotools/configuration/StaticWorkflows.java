package fr.insee.metallica.pocprotools.configuration;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.metallica.command.processor.Processors;
import fr.insee.metallica.workflow.command.processor.EnrichContextProcessor;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.service.WorkflowConfigurationService;

@Service
public class StaticWorkflows {
	static public final WorkflowDescriptor CodeConfigurationGeneratePasswordAndSendMail = WorkflowDescriptor.Builder()
			.id(UUID.fromString("eafa3d1b-644b-47dc-b803-074f08f01e2a"))
			.name("CodeConfigurationGeneratePasswordAndSendMail")
			.addStep()
				.label("generate password")
				.id(UUID.fromString("32e4b8a3-5383-4cda-8881-46c1fff7443a"))
				.type(Processors.Http)
				.addMetadatas("url", "${env.urls['password-generator']}/generate-password")
				.addMetadatas("username", "${context.username}")
				.payloadTemplate(
						"{ " +
						" \"url\": \"${metadatas.url}\",  " +
						" \"body\": { \"username\": \"${metadatas.username}\" }," +
						" \"method\": \"POST\" " +
						"}"
				).initialStep()
			.nextStep()
				.label("Add generated to context")
				.type(EnrichContextProcessor.Name)
				.addMetadatas("password", "${context.previousResult.password}")
				.payloadTemplate("{\"password\": \"${metadatas.password}\"}")
			.nextStep()
				.label("generate password")
				.id(UUID.fromString("32ecb8a3-5383-4cda-8881-46c1fff7443a"))
				.type(Processors.Http)
				.addMetadatas("url", "${env.urls['send-mail']}/send-mail")
				.addMetadatas("username", "${context.username}")
				.addMetadatas("password", "${context.password}")
				.addMetadatas("method", "POST")
				.payloadTemplate(
						"{ " +
						" \"url\": \"${metadatas.url}\",  " +
						" \"body\": { \"username\": \"${metadatas.username}\", \"password\":\"${metadatas.password}\" },  " +
						" \"method\": \"${metadatas.method}\" " +
						"}"
				).finalStep()
			.build();
	
	@Autowired
	private void workflowConfigurationService(WorkflowConfigurationService workflowConfigurationService) {
		workflowConfigurationService.addWorkflow("CodeConfigurationGeneratePasswordAndSendMail", CodeConfigurationGeneratePasswordAndSendMail);
	}
}
