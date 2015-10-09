package ru.intertrust.cm.core.model;

/**
 * @author vmatsukevich
 *         Date: 6/28/13
 *         Time: 10:48 AM
 */
public class FatalException extends RollingBackException {

    public FatalException() {
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }

    public FatalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
