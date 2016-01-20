package ru.intertrust.cm.core.gui.impl.server.plugin.handlers.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static final Logger logger = LoggerFactory.getLogger(PluginHandlerDefinitionsLoader.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    private void registerBeanDefinitionInRegistry(ScannedGenericBeanDefinition pluginHandlerDefinition, BeanDefinitionRegistry registry) {
        final String beanClassName = pluginHandlerDefinition.getBeanClassName();
        if (beanClassName.startsWith("ru.intertrust.cm.core.gui.impl.server.widget")) {
            pluginHandlerDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        } else {
            pluginHandlerDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            logger.warn(beanClassName + " is registered as prototype. Please check that it matches Singleton convention. Prototypes will be removed in future version of the Platform.");
        }
        final Map<String, Object> annotationAttributes = pluginHandlerDefinition.getMetadata().getAnnotationAttributes(ComponentName.class.getName());
        if (annotationAttributes == null) {
            return;
        }
        String annotationValue = annotationAttributes.get(VALUE_ATTRIBUTE).toString();
        registry.registerBeanDefinition(annotationValue, pluginHandlerDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ModuleService moduleService = beanFactory.getBean(ModuleService.class);    
        ScanningProvider componentProvider = new ScanningProvider();
        
        logger.info("================================================ REGISTERED COMPONENTS =========================================================");
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getGuiComponentsPackages() != null){
                for (String basePackage : moduleConfiguration.getGuiComponentsPackages()) {
    
                    Set<BeanDefinition> pluginHandlerDefinitions = componentProvider.findCandidateComponents(basePackage);
    
                    for (BeanDefinition pluginHandlerDefinition : pluginHandlerDefinitions) {
                        logger.info(pluginHandlerDefinition.getBeanClassName());
                        registerBeanDefinitionInRegistry((ScannedGenericBeanDefinition) pluginHandlerDefinition, (DefaultListableBeanFactory)beanFactory);
                    }
                }
            }
        }
        logger.info("=============================================== END REGISTERED COMPONENTS ======================================================");
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
