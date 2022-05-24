package fr.insee.metallica.pocprotools.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfiguration {
	@Bean
	public ObjectMapper mapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule()); 
	}
}

