package ru.intertrust.cm.core.gui.impl.server.plugin.handlers.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.dao.api.ClassPathScanService;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * A bean registry postprocessor which loads classes, annotated with {@link ComponentName} as bean definitions
 * to spring bean factory.
 * Target path for scanning is defined in {@link ModuleConfiguration#getGuiComponentsPackages()} field.
 */
public class PluginHandlerDefinitionsLoader {

    private static final String VALUE_ATTRIBUTE = "value";
    private static final Logger logger = LoggerFactory.getLogger(PluginHandlerDefinitionsLoader.class);

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Autowired
    private ClassPathScanService scanner;

    private void registerBeanDefinitionInRegistry(ScannedGenericBeanDefinition pluginHandlerDefinition) {
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
        ((BeanDefinitionRegistry)beanFactory).registerBeanDefinition(annotationValue, pluginHandlerDefinition);
    }

    @PostConstruct
    public void init() throws BeansException {
        Set<BeanDefinition> pluginHandlerDefinitions = scanner.findClassesByAnnotationAndSuperClass(ComponentName.class, ComponentHandler.class);
        logger.info("================================================ REGISTERED COMPONENTS =========================================================");
        for (BeanDefinition pluginHandlerDefinition : pluginHandlerDefinitions) {
            logger.info(pluginHandlerDefinition.getBeanClassName());
            registerBeanDefinitionInRegistry((ScannedGenericBeanDefinition) pluginHandlerDefinition);
        }
        logger.info("=============================================== END REGISTERED COMPONENTS ======================================================");
    }
}
