package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.model.FatalException;

/**
 * Ошибка валидации конфигурации
 * @author vmatsukevich
 *         Date: 6/19/13
 *         Time: 3:54 PM
 */
public class ConfigurationValidationException extends FatalException {

    public ConfigurationValidationException() {
    }

    public ConfigurationValidationException(String message) {
        super(message);
    }

    public ConfigurationValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationValidationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
