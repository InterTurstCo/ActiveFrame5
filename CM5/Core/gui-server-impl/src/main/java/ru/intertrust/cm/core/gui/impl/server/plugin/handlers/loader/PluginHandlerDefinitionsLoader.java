package ru.intertrust.cm.core.gui.impl.server.plugin.handlers.loader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.Set;

public class PluginHandlerDefinitionsLoader implements BeanDefinitionRegistryPostProcessor {

    public static final String VALUE_ATTRIBUTE = "value";
    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(ComponentName.class));
        Set<BeanDefinition> pluginHandlerDefinitions = componentProvider.findCandidateComponents(basePackage);

        for (BeanDefinition pluginHandlerDefinition : pluginHandlerDefinitions) {
            registerBeanDefinitionInRegistry(registry, (ScannedGenericBeanDefinition) pluginHandlerDefinition);
        }
    }

    private void registerBeanDefinitionInRegistry(BeanDefinitionRegistry registry, ScannedGenericBeanDefinition pluginHandlerDefinition) {
        ScannedGenericBeanDefinition scannedGenericBeanDefinition = pluginHandlerDefinition;
        scannedGenericBeanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        String annotationValue = scannedGenericBeanDefinition.getMetadata().getAnnotationAttributes(ComponentName.class.getName()).get(VALUE_ATTRIBUTE).toString();
        registry.registerBeanDefinition(annotationValue, scannedGenericBeanDefinition);
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no post processing
    }
}
