package ru.intertrust.cm.core.config;

/**
 * @author vmatsukevich
 *         Date: 6/19/13
 *         Time: 3:54 PM
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
