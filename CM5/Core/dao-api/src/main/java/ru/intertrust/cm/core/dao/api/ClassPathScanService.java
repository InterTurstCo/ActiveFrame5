package ru.intertrust.cm.core.dao.api;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Set;

/**
 * Сервис поиска классов по аннотации и по имени суперкласса.
 * Выполняет однократное сканирование всего classpath и находит все классы аннотированные ExtensionPoint, ComponebtName, ServerComponent, Plugin
 * и складывает во внутренний реестр. Далее при обращение за конкретной коллекцией сканирование classpath не производится.
 */
public interface ClassPathScanService{
    /**
     * Поиск классов по анотации
     * @param annotationClass
     * @return
     */
    Set<BeanDefinition> findClassesByAnnotation(Class<?> annotationClass);

    /**
     * Поик классов по анотации и базовому классу или интерфейсу
     * @param annotationClass
     * @param superClass
     * @return
     */
    Set<BeanDefinition> findClassesByAnnotationAndSuperClass(Class<?> annotationClass, Class<?> superClass);
}
