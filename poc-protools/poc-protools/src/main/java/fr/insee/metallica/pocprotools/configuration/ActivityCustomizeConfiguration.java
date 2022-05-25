package fr.insee.metallica.pocprotools.configuration;

import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.runtime.api.connector.DefaultServiceTaskBehavior;
import org.activiti.runtime.api.connector.IntegrationContextBuilder;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivityCustomizeConfiguration {
    @Bean(name = DefaultActivityBehaviorFactory.DEFAULT_SERVICE_TASK_BEAN_NAME)
    public DefaultServiceTaskBehavior defaultServiceTaskBehavior(ApplicationContext applicationContext,
                                                                 IntegrationContextBuilder integrationContextBuilder,
                                                                 VariablesMappingProvider outboundVariablesProvider) {
        return new ProtoolsDefaultServiceTaskBehavior(applicationContext, integrationContextBuilder, outboundVariablesProvider);
    }
}
