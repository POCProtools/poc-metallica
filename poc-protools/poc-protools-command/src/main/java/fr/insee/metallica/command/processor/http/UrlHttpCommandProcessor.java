package fr.insee.metallica.command.processor.http;

import java.net.URI;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.processor.Processors;
import fr.insee.metallica.command.processor.payload.UrlHttpPayload;

public class UrlHttpCommandProcessor extends AbstractHttpCommandProcessor<UrlHttpPayload> {
	public UrlHttpCommandProcessor() {
		super(Processors.Http, UrlHttpPayload.class);
	}
	
	@Override
	protected URI getUri(Command command, UrlHttpPayload payload) {
		return URI.create(payload.getUrl());
	}
}
