package ru.intertrust.cm.core.gui.impl.server.plugin.handlers.loader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A bean registry postprocessor which loads classes, annotated with {@link ComponentName} as bean definitions
 * to spring bean factory.
 * Target path for scanning is defined in {@link PluginHandlerDefinitionsLoader#basePackage} field.
 */
public class PluginHandlerDefinitionsLoader implements BeanDefinitionRegistryPostProcessor {

    private static final String VALUE_ATTRIBUTE = "value";
    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(ComponentName.class));
        componentProvider.addIncludeFilter(new AssignableTypeFilter(ComponentHandler.class));
        componentProvider.addExcludeFilter(new RegexPatternTypeFilter(Pattern.compile("ru.intertrust.cm.core.gui.api.codegen.ComponentRegistryGenerator")));
        Set<BeanDefinition> pluginHandlerDefinitions = componentProvider.findCandidateComponents(basePackage);

        for (BeanDefinition pluginHandlerDefinition : pluginHandlerDefinitions) {
            registerBeanDefinitionInRegistry((ScannedGenericBeanDefinition) pluginHandlerDefinition, registry);
        }
    }

    private void registerBeanDefinitionInRegistry(ScannedGenericBeanDefinition pluginHandlerDefinition, BeanDefinitionRegistry registry) {
        ScannedGenericBeanDefinition scannedGenericBeanDefinition = pluginHandlerDefinition;
        scannedGenericBeanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        final Map<String, Object> annotationAttributes = scannedGenericBeanDefinition.getMetadata().getAnnotationAttributes(ComponentName.class.getName());
        if (annotationAttributes == null) {
            return;
        }
        String annotationValue = annotationAttributes.get(VALUE_ATTRIBUTE).toString();
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
