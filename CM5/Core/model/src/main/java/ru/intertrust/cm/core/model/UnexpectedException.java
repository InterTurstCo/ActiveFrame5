package ru.intertrust.cm.core.model;

/**
 *
 */
public class UnexpectedException extends RollingBackException {

    public UnexpectedException() {
    }

    public UnexpectedException(String ejbName, String method, String parameters, Exception ex) {
        super(ejbName + "#" + method + " " + parameters + "\n" + ex.getClass().getName() + ":" + ex.getMessage());
    }

    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage());
    }

    public UnexpectedException(Throwable cause) {
        super(cause.getMessage());
    }

    public UnexpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this(message, cause);
    }
}
