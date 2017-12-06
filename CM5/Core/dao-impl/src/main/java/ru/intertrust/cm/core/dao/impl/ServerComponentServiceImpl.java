package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.ServerComponentService;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.dao.api.component.ServerComponentHandler;
import ru.intertrust.cm.core.model.ServerComponentException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сервис получения и инициализации серверных компонентов. Регистририрует все серверные компоненты как spring биныс с
 * видимостью (scope) - Prototype.
 * @author atsvetkov
 */
public class ServerComponentServiceImpl implements ServerComponentService {

    private static final Logger logger = LoggerFactory.getLogger(ServerComponentServiceImpl.class);
    
    private List<String> initContexts = new ArrayList<String>();
    
    private List<String> registeredComponents = new ArrayList<>();

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ApplicationContext localApplicationContext;

    @Override
    public ServerComponentHandler getServerComponent(String componentName) {
        return (ServerComponentHandler) localApplicationContext.getBean(Case.toLower(componentName));
    }

    @PostConstruct
    public void init(){
        Set<String> componentPackages = getServerComponentsPackages();
        initServerComponents(ExtensionService.PLATFORM_CONTEXT, localApplicationContext, componentPackages);
    }
    
    /**
     * Инициализация серверных компонентов, путем сканирования всех классов, аннотированых ServerComponent аннотацией.
     * Создаются соответсвующие спринг бины для серверных компонентов.
     */
    @SuppressWarnings("unchecked")
    public void initServerComponents(String contextName, ApplicationContext applicationContext, Set<String> packages) {
        try {
            for (String basePackage : packages) {

                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(ServerComponent.class));

                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    String className = bd.getBeanClassName();

                    Class<?> serverComponentClass = Class.forName(className);

                    ServerComponent annatation = (ServerComponent) serverComponentClass
                            .getAnnotation(ServerComponent.class);

                    if (annatation.context().equalsIgnoreCase(contextName)) {

                        if (annatation != null) {

                            Class<?>[] interfaces = serverComponentClass.getInterfaces();

                            for (Class<?> interfaceClass : interfaces) {
                                if (ServerComponentHandler.class.isAssignableFrom(interfaceClass)) {

                                    String serverComponentName = annatation.name();

                                    serverComponentName = toLowerCase(serverComponentName);

                                    if (registeredComponents.contains(serverComponentName)) {
                                        throw new ServerComponentException("Server component with name " + serverComponentName + " is already registered");
                                    }
                                    DefaultListableBeanFactory beanFactory =
                                            ((DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext).getBeanFactory());

                                    BeanDefinition beamDefinition =
                                            BeanDefinitionBuilder.rootBeanDefinition(serverComponentClass.getName()).
                                                    setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
                                                    .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE).getBeanDefinition();

                                    beanFactory.registerBeanDefinition(serverComponentName, beamDefinition);

                                    logger.info("Registered server component " + "(" + serverComponentName + ") = " + serverComponentClass);
                                }
                            }
                        }
                    }
                }
            }
            initContexts.add(contextName);

        } catch (Exception ex) {
            throw new ServerComponentException("Error on init extension point classes", ex);
        }
    }

    private String toLowerCase(String serverComponentName) {
        if (serverComponentName != null) {
            serverComponentName = Case.toLower(serverComponentName);
        }
        return serverComponentName;
    }

    /**
     * Получение списка пакетов для серверных компонентов.
     * @return
     */
    private Set<String> getServerComponentsPackages() {

        Set<String> result = new HashSet<String>();
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getServerComponentsPackages() != null) {
                for (String serverComponentPackage : moduleConfiguration.getServerComponentsPackages()) {
                    result.add(serverComponentPackage);
                }
            }
        }
        return result;
    }
}
