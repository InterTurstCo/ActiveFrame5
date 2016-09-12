package ru.intertrust.cm.core.model;

public class UnreversibleDoelExpressionException extends DoelException {

    public UnreversibleDoelExpressionException() {
    }

    public UnreversibleDoelExpressionException(String message) {
        super(message);
    }

    public UnreversibleDoelExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnreversibleDoelExpressionException(Throwable cause) {
        super(cause);
    }

    public UnreversibleDoelExpressionException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
