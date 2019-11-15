package ru.intertrust.cm.core.util;

import java.util.Collection;

import javax.annotation.Nonnull;

public abstract class Args {
    
    @Nonnull
    public static <X> X notNull (final X argValue, final String argName) {

        if (argValue == null) {
            throw new IllegalArgumentException(formatArgMessage(argName, "must be not-null"));
        }
        
        return argValue;
        
    }

    public static String notEmpty (final String argValue, final String argName) {

        if (argValue != null && argValue.isEmpty()) {
            throw new IllegalArgumentException(formatArgMessage(argName, "must be not-empty"));
        }

        return argValue;

    }

    public static <X extends Collection<?>> X notEmpty (final X argValue, final String argName) {

        if (argValue != null && argValue.isEmpty()) {
            throw new IllegalArgumentException(formatArgMessage(argName, "must be not-empty"));
        }

        return argValue;

    }

    public static <X extends Collection<?>> X notContainsNull (final X argValue, final String argName) {

        if (argValue != null && argValue.contains(null)) {
            throw new IllegalArgumentException(formatArgMessage(argName, "can't contains null-value(s)"));
        }

        return argValue;

    }

    private static String formatArgMessage (final String argName, final String message) {
        return "Argument " + (argName == null || argName.isEmpty() ? "" : "'" + argName + "' ") + message;
    }
    
    private Args () {
    }

}