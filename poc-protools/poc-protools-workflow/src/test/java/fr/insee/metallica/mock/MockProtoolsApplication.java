package fr.insee.metallica.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.insee.metallica.workflow.configuration.EnableWorkflow;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableWorkflow
public class MockProtoolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockProtoolsApplication.class, args);
	}

}
