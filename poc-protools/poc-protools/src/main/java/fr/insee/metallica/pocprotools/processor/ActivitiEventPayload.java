package fr.insee.metallica.pocprotools.processor;

import java.util.Map;

public class ActivitiEventPayload {
	private String name;
	private Map<String, Object> context;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getContext() {
		return context;
	}
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}
