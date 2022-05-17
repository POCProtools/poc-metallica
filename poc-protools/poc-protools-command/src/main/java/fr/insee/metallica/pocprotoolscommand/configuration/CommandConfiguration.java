package fr.insee.metallica.pocprotoolscommand.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.processor.DoNothingCommandProcessor;
import fr.insee.metallica.pocprotoolscommand.processor.PrintCommandProcessor;
import fr.insee.metallica.pocprotoolscommand.processor.http.ServiceHttpCommandProcessor;
import fr.insee.metallica.pocprotoolscommand.processor.http.UrlHttpCommandProcessor;
import fr.insee.metallica.pocprotoolscommand.repository.CommandRepository;
import fr.insee.metallica.pocprotoolscommand.service.CommandEngine;
import fr.insee.metallica.pocprotoolscommand.service.CommandScheduler;
import fr.insee.metallica.pocprotoolscommand.service.CommandService;

@EnableTransactionManagement
@EnableScheduling
@EntityScan(basePackageClasses = Command.class)
@EnableJpaRepositories(basePackageClasses = CommandRepository.class)
public class CommandConfiguration {
	@Bean
	public CommandEngine commandEngine() {
		return new CommandEngine(); 
	}

	@Bean
	public CommandService commandService() {
		return new CommandService(); 
	}
	
	@Bean
	public DoNothingCommandProcessor doNothingCommandProcessor() {
		return new DoNothingCommandProcessor();	
	}
	
	@Bean
	public PrintCommandProcessor printCommandProcessor() {
		return new PrintCommandProcessor();	
	}
	
	@Bean
	public ServiceHttpCommandProcessor serviceHttpCommandProcessor() {
		return new ServiceHttpCommandProcessor();	
	}
	
	@Bean
	public CommandScheduler commandScheduler() {
		return new CommandScheduler();	
	}

	@Bean
	public UrlHttpCommandProcessor urlHttpCommandProcessor() {
		return new UrlHttpCommandProcessor();	
	}
	
	@Bean
	public CommandProperties commandProperties() {
		return new CommandProperties();
	}
}
