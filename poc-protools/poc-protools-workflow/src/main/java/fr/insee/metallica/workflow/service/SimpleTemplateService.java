package fr.insee.metallica.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SimpleTemplateService {
	public static class NodeObjectAccessor implements PropertyAccessor {
		@Override
		public Class<?>[] getSpecificTargetClasses() {
			return new Class<?> [] {ObjectNode.class};
		}

		@Override
		public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
			return true;
		}

		@Override
		public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
			var node = (ObjectNode) target;
			return !node.has(name) ? new TypedValue(null) : 
				node.get(name).isTextual() ? new TypedValue(node.get(name).asText()):
						new TypedValue(node.get(name));
		}

		@Override
		public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
			return false;
		}

		@Override
		public void write(EvaluationContext context, Object target, String name, Object newValue)
				throws AccessException {
		}
		
	}
	
	public class ExpressionRoot {
		private Object context;
		private Object metadatas;
		
		public ExpressionRoot(Object context, Object metadatas) {
			this.context = context;
			this.metadatas = metadatas;
		}
		public Object getContext() {
			return context;
		}
		public void setContext(Object context) {
			this.context = context;
		}
		public Object getMetadatas() {
			return metadatas;
		}
		public void setMetadatas(Object metadatas) {
			this.metadatas = metadatas;
		}
		
		public String JSON(Object o) throws JsonProcessingException {
			return mapper.writeValueAsString(o);
		}
	}
	
	private ParserContext context = new TemplateParserContext("${", "}");
	private ExpressionParser expressionParser = new SpelExpressionParser();
	
	@Autowired
	private ObjectMapper mapper;
	
	public Expression parseTemplate(String exp) {
		return expressionParser.parseExpression(exp, context);
	}

	public Object evaluate(Expression exp, Object context, Object metadatas) {
		var root = new ExpressionRoot(context, metadatas);
		var evaluationContext = new SimpleEvaluationContext.Builder(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor(), new NodeObjectAccessor())
			.withRootObject(root)
			.withInstanceMethods()
			.build();
		
		return exp.getValue(evaluationContext);
	}
	
	public Object evaluateTemplate(String template, Object context, Object metadatas) {
		if (template == null) {
			return null;
		}
		return evaluate(parseTemplate(template), context, metadatas);
	}

}
