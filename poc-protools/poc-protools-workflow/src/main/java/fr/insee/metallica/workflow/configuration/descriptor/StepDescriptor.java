package fr.insee.metallica.workflow.configuration.descriptor;

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

	private StepDescriptor asyncResult;

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
	
	public StepDescriptor getAsyncResult() {
		return asyncResult;
	}

	private StepDescriptor() {
	}
	
	public static WorkflowStepBuilder Builder(WorkflowDescriptor.Builder workflowBuilder) {
		return new WorkflowStepBuilder(workflowBuilder);
	}
	
	public interface StepBuilderContract<T extends StepBuilderContract<?>> {
		T id(UUID stepId);
		T label(String label);
		T payloadTemplate(String layoutTemplate);
		T type(String type);
		T addMetadatas(String key, Object value);
		T limit(String limitKey, int limit);
	}
	
	public static class WorkflowStepBuilder extends StepBuilderBase<WorkflowStepBuilder> {
		private WorkflowDescriptor.Builder workflowBuilder;
		
		@Override
		protected WorkflowStepBuilder getThis() {
			return this;
		}

		private WorkflowStepBuilder(WorkflowDescriptor.Builder workflowBuilder) {
			this.workflowBuilder = workflowBuilder;
		}
		
		public WorkflowStepBuilder nextStep() {
			workflowBuilder.endStep(descriptor);
			var builder = new StepDescriptor.WorkflowStepBuilder(workflowBuilder);
			this.descriptor.nextStep = builder.descriptor;
			return builder;
		}
		
		public WorkflowStepBuilder asyncResult(StepDescriptor stepDescriptor) {
			descriptor.asyncResult = stepDescriptor;
			return getThis();
		}
		
		public WorkflowStepBuilder initialStep() {
			descriptor.initialStep = true;
			return getThis();
		}
		
		public WorkflowStepBuilder finalStep() {
			descriptor.finalStep = true;
			return getThis();
		}
		
		public WorkflowDescriptor build() {
			workflowBuilder.endStep(descriptor);
			return workflowBuilder.build();
		}

	}
	
	public static class StepBuilder extends StepBuilderBase<StepBuilder> {
		@Override
		protected StepBuilder getThis() {
			return this;
		}
		
		public StepDescriptor build() {
			return this.descriptor;
		}
	}
	
	public abstract static class StepBuilderBase<T extends StepBuilderContract<?>> implements StepBuilderContract<T> {
		protected final StepDescriptor descriptor = new StepDescriptor();
		protected abstract T getThis();
		
		@Override
		public T id(UUID stepId) {
			descriptor.id = stepId;
			return getThis();
		}
		
		@Override
		public T label(String label) {
			descriptor.label = label;
			return getThis();
		}
		
		@Override
		public T payloadTemplate(String layoutTemplate) {
			descriptor.payloadTemplate = layoutTemplate;
			return getThis();
		}
		
		@Override
		public T type(String type) {
			descriptor.type = type;
			return getThis();
		}
		
		@Override
		public T addMetadatas(String key, Object value) {
			if (descriptor.metadatas.put(key, value) != null) {
				throw new RuntimeException("Cannot add multiple metadatas with the same key " + key);
			}
			return getThis();
		}
		
		@Override
		public T limit(String limitKey, int limit) {
			descriptor.limit = limit;
			descriptor.limitKey = limitKey;
			return getThis();
		}
	}
}
