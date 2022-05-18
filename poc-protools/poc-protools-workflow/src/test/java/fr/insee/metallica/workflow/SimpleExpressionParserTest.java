package fr.insee.metallica.workflow;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import fr.insee.metallica.mock.MockProtoolsApplication;
import fr.insee.metallica.workflow.configuration.EnvironmentProperties;
import fr.insee.metallica.workflow.service.SimpleTemplateService;

@SpringBootTest(classes = MockProtoolsApplication.class)
public class SimpleExpressionParserTest {
	@Autowired
	private SimpleTemplateService simpleTemplateParser;
	
	@Autowired
	private EnvironmentProperties environmentProperties;

	@Test
	public void testParse() {
		var exp = simpleTemplateParser.parseTemplate("${env['enviro']}");
		var root = new HashMap<String, Object>();
		
		root.put("env", environmentProperties.getEnv());
		var evaluationContext = new SimpleEvaluationContext.Builder(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();
		
		System.out.println(exp.getValue(evaluationContext));
	}
}
