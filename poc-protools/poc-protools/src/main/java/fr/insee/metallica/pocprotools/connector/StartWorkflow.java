package fr.insee.metallica.pocprotools.connector;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.stereotype.Service;

@Service
public class StartWorkflow implements Connector {

	@Override
	public IntegrationContext apply(IntegrationContext t) {
		System.out.println("ca marche");
		return null;
	}

}
