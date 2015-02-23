package ru.intertrust.cm.core.business.api;

import java.lang.annotation.*;

/**
 * Аннотация миграционного компонента. Содержит имя компонента
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MigrationComponent {
    String name();
}
