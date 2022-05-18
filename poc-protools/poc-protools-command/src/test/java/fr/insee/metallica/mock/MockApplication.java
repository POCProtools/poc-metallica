package fr.insee.metallica.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import fr.insee.metallica.command.configuration.EnableCommand;

@SpringBootApplication
@EnableCommand
@EnableWebMvc
public class MockApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockApplication.class, args);
	}

}
