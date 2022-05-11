package fr.insee.metallica.pocprotools.command.processor.http;

import org.springframework.stereotype.Service;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.command.processor.Processors;
import fr.insee.metallica.pocprotools.command.processor.payload.UrlHttpPayload;

@Service
public class UrlHttpCommandProcessor extends AbstractHttpCommandProcessor<UrlHttpPayload> {
	public UrlHttpCommandProcessor() {
		super(Processors.Http, UrlHttpPayload.class);
	}
	 @Override
	protected String getUrl(Command command, UrlHttpPayload payload) {
		return payload.getUrl();
	}
}
