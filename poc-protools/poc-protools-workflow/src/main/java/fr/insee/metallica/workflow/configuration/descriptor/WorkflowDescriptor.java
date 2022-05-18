package fr.insee.metallica.workflow.configuration.descriptor;

import java.util.HashMap;
import java.util.UUID;

public class WorkflowDescriptor {
	private UUID id;
	
	private String name;
	
	private StepDescriptor initialStep;
	
	private StepDescriptor finalStep;
	
	private HashMap<UUID, StepDescriptor> steps = new HashMap<>();
	
	public UUID getId() {
		return id;
	}

	public StepDescriptor getInitialStep() {
		return initialStep;
	}
	
	public StepDescriptor getFinalStep() {
		return finalStep;
	}

	public StepDescriptor getStep(UUID stepId) {
		return steps.get(stepId);
	}
	
	public String getName() {
		return name;
	}

	private WorkflowDescriptor() {
	}
	
	public static Builder Builder() {
		return new Builder();
	}
	
	public static class Builder {
		private WorkflowDescriptor descriptor;

		private Builder() {
			this.descriptor = new WorkflowDescriptor();
		}
		
		public WorkflowDescriptor build() {
			if (descriptor.initialStep == null || descriptor.finalStep == null) {
				throw new RuntimeException("Workflow must have one initial step and one final step");
			}
			return descriptor;
		}
		
		public Builder id(UUID workflowId) {
			descriptor.id = workflowId;
			return this;
		}
		
		public Builder name(String name) {
			descriptor.name = name;
			return this;
		}

		public StepDescriptor.WorkflowStepBuilder addStep() {
			return StepDescriptor.Builder(this);
		}
		
		Builder endStep(StepDescriptor step) {
			descriptor.steps.put(step.getId(), step);
			if (step.isInitialStep()) {
				if (descriptor.initialStep != null) {
					throw new RuntimeException("Impossible two initial steps");
				}
				this.descriptor.initialStep = step;
			}
			if (step.isFinalStep()) {
				if (descriptor.finalStep != null) {
					throw new RuntimeException("Impossible two final steps");
				}
				this.descriptor.finalStep = step;
			}
			return this;
		}
	}
}
