package fr.insee.metallica.command.processor.http;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.command.configuration.CommandProperties;
import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.processor.Processors;
import fr.insee.metallica.command.processor.payload.ServiceHttpPayload;

public class ServiceHttpCommandProcessor extends AbstractHttpCommandProcessor<ServiceHttpPayload> {
	@Autowired
	private CommandProperties commandProperties;
	
	
	public ServiceHttpCommandProcessor() {
		super(Processors.ServiceHttp, ServiceHttpPayload.class);
	}
	
	@Override
	protected URI getUri(Command command, ServiceHttpPayload payload) {
		return URI.create(commandProperties.getServices().get(payload.getService()) + payload.getPath());
	}
}
