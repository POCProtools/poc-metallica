package fr.insee.metallica.pocprotools.controller;

import java.util.Map;
import java.util.UUID;

import fr.insee.metallica.pocprotools.service.processor.EnrichContextProcessor;
import fr.insee.metallica.pocprotoolscommand.processor.Processors;

public class Workflows {
	static public final WorkflowDescriptor GeneratePasswordAndSendMail = WorkflowDescriptor.Builder()
			.id(UUID.fromString("eafa3d1b-644b-47dc-b803-074f08f01e2a"))
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
	
	static public Map<UUID, WorkflowDescriptor> Workflows = Map.of(
			GeneratePasswordAndSendMail.getId(), GeneratePasswordAndSendMail
	);
}
