package fr.insee.metallica.pocprotools.command.processor.http;

import org.springframework.stereotype.Service;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.command.processor.Processors;
import fr.insee.metallica.pocprotools.command.processor.payload.ServiceHttpPayload;

@Service
public class ServiceHttpCommandProcessor extends AbstractHttpCommandProcessor<ServiceHttpPayload> {
	public ServiceHttpCommandProcessor() {
		super(Processors.ServiceHttp, ServiceHttpPayload.class);
	}
	 @Override
	protected String getUrl(Command command, ServiceHttpPayload payload) {
		return payload.getUri();
	}
}
