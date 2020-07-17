package ru.intertrust.cm.core.gui.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Аннотация, определяющая имя компонента GUI
 * @author Denis Mitavskiy
 *         Date: 22.07.13
 *         Time: 17:30
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
public @interface ComponentName {
    String value();
}
