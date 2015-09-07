package ru.intertrust.cm.core.config.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.08.2015
 *         Time: 0:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotNullLogicalValidation {
    String[] skippedComponentNames() default "";
}
