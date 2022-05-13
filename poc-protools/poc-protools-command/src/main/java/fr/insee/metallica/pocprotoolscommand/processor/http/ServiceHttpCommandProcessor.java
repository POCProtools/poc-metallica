package fr.insee.metallica.pocprotoolscommand.processor.http;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.metallica.pocprotoolscommand.configuration.CommandProperties;
import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.processor.Processors;
import fr.insee.metallica.pocprotoolscommand.processor.payload.ServiceHttpPayload;

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
