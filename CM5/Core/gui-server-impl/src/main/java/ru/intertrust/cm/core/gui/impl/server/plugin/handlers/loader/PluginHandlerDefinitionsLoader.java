package ru.intertrust.cm.core.gui.impl.server.plugin.handlers.loader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.io.IOException;
import java.util.*;
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
        ScanningProvider componentProvider = new ScanningProvider();
        Set<BeanDefinition> pluginHandlerDefinitions = componentProvider.findCandidateComponents(basePackage);

        System.out.println("================================================ REGISTERED COMPONENTS =========================================================");
        for (BeanDefinition pluginHandlerDefinition : pluginHandlerDefinitions) {
            System.out.println(pluginHandlerDefinition.getBeanClassName());
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

    /**
     * Custom scanner - implementing AND-logic of filtering instead of Spring's OR-logic
     */
    private static class ScanningProvider extends ClassPathScanningCandidateComponentProvider {
        private MetadataReaderFactory metadataReaderFactory;
        private List<TypeFilter> includeFilters = new ArrayList<>(2);
        private List<TypeFilter> excludeFilters = new ArrayList<>(2);

        public ScanningProvider() {
            super(false);
            metadataReaderFactory = getMetadataReaderFactory();
            this.includeFilters.add(new AnnotationTypeFilter(ComponentName.class));
            this.includeFilters.add(new AssignableTypeFilter(ComponentHandler.class));
            this.excludeFilters.add(new RegexPatternTypeFilter(Pattern.compile("ru.intertrust.cm.core.gui.api.codegen.ComponentRegistryGenerator")));
        }

        /**
         * Determine whether the given class does not match any exclude filter
         * and does match at least one include filter.
         * @param metadataReader the ASM ClassReader for the class
         * @return whether the class qualifies as a candidate component
         */
        protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
            for (TypeFilter tf : this.excludeFilters) {
                if (tf.match(metadataReader, this.metadataReaderFactory)) {
                    return false;
                }
            }
            for (TypeFilter tf : this.includeFilters) {
                if (!tf.match(metadataReader, this.metadataReaderFactory)) {
                    return false;
                }
            }
            return true;
        }
    }
}
