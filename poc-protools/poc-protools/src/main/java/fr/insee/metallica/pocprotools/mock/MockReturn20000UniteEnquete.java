package fr.insee.metallica.pocprotools.mock;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import fr.insee.metallica.command.domain.Command;
import fr.insee.metallica.command.exception.CommandExecutionException;
import fr.insee.metallica.command.processor.AbstractCommandProcessor;
import fr.insee.metallica.pocprotools.unite.UniteEnquete;

@Service
public class MockReturn20000UniteEnquete extends AbstractCommandProcessor {
	public MockReturn20000UniteEnquete() {
		super("MockReturn20000UniteEnquete");
	}

	@Override
	public Object process(Command command) throws CommandExecutionException {
		return Stream.generate(UniteEnquete::random).limit(20000).collect(Collectors.toList());
	}
}
