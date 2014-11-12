package ru.intertrust.cm.core.config.doel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import ru.intertrust.cm.core.model.DoelException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.annotation.PostConstruct;
import java.util.HashMap;

public class DoelFunctionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DoelFunctionRegistry.class);

    private HashMap<String, Class<?>> functionMap = new HashMap<>();

    @PostConstruct
    private void initialize() {
        //TODO Get the path from cm-module.xmls
        String basePackage = "ru/intertrust/cm";
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DoelFunction.class));
        for (BeanDefinition beanDef : scanner.findCandidateComponents(basePackage)) {
            String className = beanDef.getBeanClassName();
            Class<?> functionClass;
            try {
                functionClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                logger.error("Error loading function class " + className, e);
                continue;
            }
            DoelFunction functionAnnotation = functionClass.getAnnotation(DoelFunction.class);
            String functionName = functionAnnotation.name().toLowerCase();
            if (functionMap.containsKey(functionName)) {
                //TODO support function overriding for different context types
                logger.error("Duplicate function definition: " + functionName + " [" + className + "]");
                continue;
            }
            functionMap.put(functionName, functionClass);
        }
    }

    public DoelFunctionValidator getFunctionValidator(String name) {
        name = name.toLowerCase();
        if (!functionMap.containsKey(name)) {
            throw new IllegalArgumentException("Function " + name + " not defined");
        }
        Class<?> clazz = functionMap.get(name);
        if (DoelFunctionValidator.class.isAssignableFrom(clazz)) {
            return getInstance(name);
        }
        return new AnnotationFunctionValidator(clazz.getAnnotation(DoelFunction.class));
    }

    public <T> T getFunctionImplementation(String name) {
        name = name.toLowerCase();
        if (!functionMap.containsKey(name)) {
            throw new IllegalArgumentException("Function " + name + " not defined");
        }
        T implementation = getInstance(name);
        SpringApplicationContext.getContext().getAutowireCapableBeanFactory().initializeBean(implementation, name);
        return implementation;
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(String name) {
        Class<?> clazz = functionMap.get(name);
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            logger.error("Error instantiating function class" + clazz.getName(), e);
            throw new DoelException("Error instantiating function class" + clazz.getName(), e);
        }
    }
}
