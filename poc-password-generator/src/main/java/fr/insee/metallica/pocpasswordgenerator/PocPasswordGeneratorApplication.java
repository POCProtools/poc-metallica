package fr.insee.metallica.pocpasswordgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.insee.metallica.outbox.configuration.EnableOutbox;
import fr.insee.metallica.pocpasswordgenerator.domain.PasswordHash;
import fr.insee.metallica.pocpasswordgenerator.repository.PasswordHashRepository;

@SpringBootApplication
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
@EnableOutbox
@EntityScan(basePackageClasses = PasswordHash.class)
@EnableJpaRepositories(basePackageClasses = PasswordHashRepository.class)
public class PocPasswordGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocPasswordGeneratorApplication.class, args);
	}

}
