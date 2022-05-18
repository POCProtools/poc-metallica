package fr.insee.metallica.pocprotools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.insee.metallica.workflow.configuration.EnableWorkflow;

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(basePackageClasses = PocProtoolsApplication.class)
@EnableTransactionManagement
@EnableWorkflow
@EntityScan(basePackageClasses = PocProtoolsApplication.class)
public class PocProtoolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocProtoolsApplication.class, args);
	}

}
