package fr.insee.metallica.workflow.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.metallica.workflow.configuration.descriptor.StepDescriptor;
import fr.insee.metallica.workflow.configuration.descriptor.WorkflowDescriptor;
import fr.insee.metallica.workflow.configuration.descriptor.StepDescriptor.StepBuilder;
import fr.insee.metallica.workflow.configuration.descriptor.StepDescriptor.StepBuilderBase;

/**
 * to parse the workflows.yaml file
 * @author jhaderer
 *
 */
public class WorkflowsProperties {
	private List<WorkflowProperties> workflows;

	public static class WorkflowProperties { 
		private UUID id;
		private String name;
		private List<WorkflowStepProperties> steps;
		public UUID getId() {
			return id;
		}
		public void setId(UUID id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<WorkflowStepProperties> getSteps() {
			return steps;
		}
		public void setSteps(List<WorkflowStepProperties> steps) {
			this.steps = steps;
		}
		
		public WorkflowDescriptor toWorkflowDescriptors(String name) {
			var builder = WorkflowDescriptor.Builder()
				.id(id)
				.name(name);
			
			if (steps.isEmpty()) {
				return builder.build();
			}
			var stepBuilder = builder
			.addStep()
			.initialStep();
			
			var isFirst = true;
			for (var step : steps) {
				if (!isFirst) {
					stepBuilder = stepBuilder.nextStep();
				} else {
					isFirst = false;
				}
				step.toStepDescriptors(stepBuilder);
				
			}
			stepBuilder.finalStep();
			
			return stepBuilder.build();
		}
	}
	
	public static class WorkflowStepProperties {
		UUID id;
		String label;
		String type;
		int limit;
		String limitKey;
	    String payloadTemplate;
	    
		@JsonProperty(required = false)
		Map<String, String> metadatas = new HashMap<>();
	    @JsonProperty(required = false)
	    WorkflowStepProperties asyncResult;

		public UUID getId() {
			return id;
		}
		
		private void commonBuild(StepBuilderBase<?> b) {
			b.id(id)
			.label(label)
			.type(type)
			.limit(limitKey, limit)
			.payloadTemplate(payloadTemplate);
						
			metadatas.forEach(b::addMetadatas);
		}
		
		public void toStepDescriptors(StepDescriptor.WorkflowStepBuilder b) {
			commonBuild(b);
			if (asyncResult != null) {
				b.asyncResult(asyncResult.toStepDescriptors());
			}
		}
		
		private StepDescriptor toStepDescriptors() {
			StepBuilder b = new StepBuilder();
			commonBuild(b);			
			return b.build();
		}
		
		public void setId(UUID id) {
			this.id = id;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Map<String, String> getMetadatas() {
			return metadatas;
		}
		public void setMetadatas(Map<String, String> metadatas) {
			this.metadatas = metadatas;
		}
		public String getPayloadTemplate() {
			return payloadTemplate;
		}
		public void setPayloadTemplate(String payloadTemplate) {
			this.payloadTemplate = payloadTemplate;
		}
		public int getLimit() {
			return limit;
		}
		public void setLimit(int limit) {
			this.limit = limit;
		}
		public String getLimitKey() {
			return limitKey;
		}
		public void setLimitKey(String limitKey) {
			this.limitKey = limitKey;
		}
		
	}

	public List<WorkflowProperties> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<WorkflowProperties> workflows) {
		this.workflows = workflows;
	}
	
	public Map<String, WorkflowDescriptor> toWorkflowDescriptors() {
		return workflows.stream().collect(Collectors.toMap(
			WorkflowProperties::getName,								
			w -> w.toWorkflowDescriptors(w.getName())
		));
	}
	
	
}
