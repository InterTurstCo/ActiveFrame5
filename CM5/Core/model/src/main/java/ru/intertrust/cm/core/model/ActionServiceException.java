package ru.intertrust.cm.core.model;

public class ActionServiceException extends NonRollingBackException {
    private static final long serialVersionUID = 3814955429458773118L;

    public ActionServiceException() {
    }

    public ActionServiceException(String message) {
        super(message);
    }

    public ActionServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionServiceException(Throwable cause) {
        super(cause);
    }

    public ActionServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
