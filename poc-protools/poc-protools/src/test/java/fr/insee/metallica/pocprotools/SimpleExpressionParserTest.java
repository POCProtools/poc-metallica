package fr.insee.metallica.pocprotools;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import fr.insee.metallica.pocprotools.configuration.EnvironmentProperties;
import fr.insee.metallica.pocprotools.service.SimpleTemplateService;

@SpringBootTest
public class SimpleExpressionParserTest {
	@Autowired
	private SimpleTemplateService simpleTemplateParser;
	
	@Autowired
	private EnvironmentProperties environmentProperties;

	@Test
	public void testParse() {
		var exp = simpleTemplateParser.parseTemplate("${env.urls['password-generator']}/generate-password");
		var root = new HashMap<String, Object>();
		
		root.put("env", environmentProperties);
		var evaluationContext = new SimpleEvaluationContext.Builder(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();
		
		System.out.println(exp.getValue(evaluationContext));
	}
}
