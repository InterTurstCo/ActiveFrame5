package ru.intertrust.cm.core.model;

/**
 *
 */
public class UnexpectedException extends SystemException {

    public UnexpectedException() {
    }

    public UnexpectedException(String ejbName, String method, String parameters, Exception ex) {
        super(ejbName + "#" + method + " " + parameters + "\n" + ex.getClass().getName() + ":" + ex.getMessage(), ex);
    }

    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedException(Throwable cause) {
        super(cause);
    }

    public UnexpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
