package fr.insee.metallica.pocprotools.controller;

import java.util.UUID;

public interface StepBuilderContract<T extends StepBuilderContract<?>> {
	T id(UUID stepId);
	T label(String label);
	T payloadTemplate(String layoutTemplate);
	T type(String type);
	T addMetadatas(String key, Object value);
	T limit(String limitKey, int limit);
}