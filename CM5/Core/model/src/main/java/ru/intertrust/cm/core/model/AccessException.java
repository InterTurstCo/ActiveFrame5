package ru.intertrust.cm.core.model;

import ru.intertrust.cm.core.model.SystemException;

public class AccessException extends SystemException {

    public AccessException() {
        super();
    }

    public AccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessException(String message) {
        super(message);
    }

    public AccessException(Throwable cause) {
        super(cause);
    }
}
