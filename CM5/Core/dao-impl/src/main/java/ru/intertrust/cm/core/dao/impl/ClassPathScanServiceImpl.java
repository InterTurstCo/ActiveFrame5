package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.api.ClassPathScanService;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сервис поиска классов по аннотации и по имени суперкласса.
 * Выполняет однократное сканирование всего classpath и находит все классы аннотированные ExtensionPoint, ComponebtName, ServerComponent, Plugin
 * и складывает во внутренний реестр. Далее при обращение за конкретной коллекцией сканирование classpath не производится.
 */
public class ClassPathScanServiceImpl implements ClassPathScanService {
    private static final Logger logger = LoggerFactory.getLogger(ClassPathScanServiceImpl.class);

    @Autowired
    private List<ClassPathScanConfig> classPathScanConfigs;

    @Autowired
    private ModuleService moduleService;

    private Set<BeanDefinition> classReestr = new HashSet<>();

    @PostConstruct
    public void init() {
        Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();
        for (ClassPathScanConfig classPathScanConfig : classPathScanConfigs) {
            annotationClasses.addAll(classPathScanConfig.getAnnotationList());
        }

        logger.info("Start scan");
        for (String basePackage : getBasePackages()) {

            logger.info("Find Component in " + basePackage);
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
            }

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                logger.info("Find " + beanDefinition.getBeanClassName());
                classReestr.add(beanDefinition);
            }
        }
        logger.info("End scan");
    }

    @Override
    public Set<BeanDefinition> findClassesByAnnotation(Class<?> annotationClass) {
        Set<BeanDefinition> result = new HashSet<>();
        for (BeanDefinition beanDefinition : classReestr) {
            if (((ScannedGenericBeanDefinition) beanDefinition).getMetadata().hasAnnotation(annotationClass.getName())) {
                result.add(beanDefinition);
            }
        }
        return result;
    }

    @Override
    public Set<BeanDefinition> findClassesByAnnotationAndSuperClass(Class<?> annotationClass, Class<?> superClass) {
        Set<BeanDefinition> result = new HashSet<>();
        for (BeanDefinition beanDefinition : classReestr) {

            if (((ScannedGenericBeanDefinition) beanDefinition).getMetadata().hasAnnotation(annotationClass.getName())
                    && isAssignableFrom(((ScannedGenericBeanDefinition) beanDefinition).getBeanClassName(), superClass)) {
                result.add(beanDefinition);
            }
        }
        return result;
    }

    public boolean isAssignableFrom(String className, Class<?> superClass) {
        try {
            Class<?> beanClass = getClass().getClassLoader().loadClass(className);
            return superClass.isAssignableFrom(beanClass);
        }catch(ClassNotFoundException ex){
            throw new FatalException("Error check superclass", ex);
        }
    }

    /**
     * Получение уникального списка пакетов для поиска классов
     *
     * @return
     */
    private Set<String> getBasePackages() {
        // Собираем уникальные значения
        Set<String> result = new HashSet<>();
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getGuiComponentsPackages() != null) {
                for (String basePackage : moduleConfiguration.getGuiComponentsPackages()) {
                    result.add(basePackage);
                }
            }
            if (moduleConfiguration.getExtensionPointsPackages() != null) {
                for (String basePackage : moduleConfiguration.getExtensionPointsPackages()) {
                    result.add(basePackage);
                }
            }
            if (moduleConfiguration.getServerComponentsPackages() != null) {
                for (String basePackage : moduleConfiguration.getServerComponentsPackages()) {
                    result.add(basePackage);
                }
            }
        }

        // Удаляем уточнения пакетов, например если в настройке есть  ru.intertrust.gui и ru.intertrust.gui.server, то оставляем только ru.intertrust.gui
        // Сначала ищем то что лишнее
        Set<String> foreDelete = new HashSet<>();
        for (String basePackage : result) {
            for (String checkBasePackage : result) {
                if (!basePackage.equals(checkBasePackage)) {
                    if (checkBasePackage.startsWith(basePackage)) {
                        foreDelete.add(checkBasePackage);
                    }
                }
            }
        }

        // Теперь удаляем
        for (String basePackage : foreDelete) {
            result.remove(basePackage);
        }

        return result;
    }

}
