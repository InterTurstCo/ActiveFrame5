package ru.intertrust.cm.core.business.api.notification;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface NotificationChannel {
    /**
     * имя канала
     * @return
     */
    String name();

    /**
     * Описание канала
     * @return
     */
    String description();
}
