package ru.intertrust.cm.core.business.api.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ru.intertrust.cm.core.dao.api.ExtensionService;

/**
 * Анотация к плагину
 * @author larin
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface Plugin {
    String name();
    String description();
    boolean autostart() default false;    
    String context() default ExtensionService.PLATFORM_CONTEXT;
    boolean transactional() default true;
}
