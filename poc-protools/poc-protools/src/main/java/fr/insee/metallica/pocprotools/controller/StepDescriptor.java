package fr.insee.metallica.pocprotools.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StepDescriptor {
	private UUID id;
	
	private String label;
	
	private String type;
	
	private Map<String, Object> metadatas = new HashMap<>();
	
	private boolean initialStep;
	
	private boolean finalStep;
	
	private String payloadTemplate;
	
	private StepDescriptor nextStep;

	private int limit;
	
	private String limitKey;

	public UUID getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getType() {
		return type;
	}

	public Map<String, Object> getMetadatas() {
		return metadatas;
	}

	public boolean isInitialStep() {
		return initialStep;
	}

	public boolean isFinalStep() {
		return finalStep;
	}

	public StepDescriptor getNextStep() {
		return nextStep;
	}
	
	public String getPayloadTemplate() {
		return payloadTemplate;
	}
	
	public int getLimit() {
		return limit;
	}

	public String getLimitKey() {
		return limitKey;
	}

	private StepDescriptor() {
	}
	
	public static Builder Builder(WorkflowDescriptor.Builder workflowBuilder) {
		return new Builder(workflowBuilder);
	}
	
	public static class Builder {
		private StepDescriptor descriptor;
		private WorkflowDescriptor.Builder workflowBuilder;

		private Builder(WorkflowDescriptor.Builder workflowBuilder) {
			this.descriptor = new StepDescriptor();
			this.workflowBuilder = workflowBuilder;
		}
		
		public Builder id(UUID stepId) {
			descriptor.id = stepId;
			return this;
		}
		
		public Builder label(String label) {
			descriptor.label = label;
			return this;
		}
		
		public Builder payloadTemplate(String layoutTemplate) {
			descriptor.payloadTemplate = layoutTemplate;
			return this;
		}
		
		public Builder type(String type) {
			descriptor.type = type;
			return this;
		}
		
		public Builder addMetadatas(String key, Object value) {
			if (descriptor.metadatas.put(key, value) != null) {
				throw new RuntimeException("Cannot add multiple metadatas with the same key " + key);
			}
			return this;
		}
		
		public Builder initialStep() {
			descriptor.initialStep = true;
			return this;
		}
		
		public Builder finalStep() {
			descriptor.finalStep = true;
			return this;
		}
		
		public Builder limit(String limitKey, int limit) {
			descriptor.limit = limit;
			descriptor.limitKey = limitKey;
			return this;
		}

		public Builder nextStep() {
			workflowBuilder.endStep(descriptor);
			var builder = new StepDescriptor.Builder(workflowBuilder);
			this.descriptor.nextStep = builder.descriptor;
			return builder;
		}
		
		public WorkflowDescriptor build() {
			workflowBuilder.endStep(descriptor);
			return workflowBuilder.build();
		}

	}
}
