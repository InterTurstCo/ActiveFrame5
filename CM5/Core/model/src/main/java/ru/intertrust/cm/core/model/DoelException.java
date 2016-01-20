package ru.intertrust.cm.core.model;

public class DoelException extends NonRollingBackException {
    
    private static final long serialVersionUID = -2230300132285018962L;

    public DoelException() {
    }

    public DoelException(String message) {
        super(message);
    }

    public DoelException(String message, Throwable cause) {
        super(message, cause);
    }

    public DoelException(Throwable cause) {
        super(cause);
    }

    public DoelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
