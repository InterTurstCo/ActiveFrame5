package ru.intertrust.cm.core.dao.api.component;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ru.intertrust.cm.core.dao.api.ExtensionService;

/**
 * Класс аннотации для серверных компонентов.
 
 * @author atsvetkov
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface ServerComponent {
    /**
     * Имя серверного компонента, который будет использоваться в конфигурации.
     * @return
     */
    String name();
    String context() default ExtensionService.PLATFORM_CONTEXT;
}
