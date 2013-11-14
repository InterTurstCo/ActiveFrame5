package ru.intertrust.cm.core.dao.api.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Класс аннотации для точек расширения
 * 
 * @author larin
 * 
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface ExtensionPoint {
    String filter() default "";
}