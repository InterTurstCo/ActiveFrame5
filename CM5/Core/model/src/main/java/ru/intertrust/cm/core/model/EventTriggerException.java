package ru.intertrust.cm.core.model;

/**
 * 
 * @author atsvetkov
 *
 */
public class EventTriggerException extends NonRollingBackException {

    private static final long serialVersionUID = 3814955429458773118L;

    public EventTriggerException() {
    }

    public EventTriggerException(String message) {
        super(message);
    }

    public EventTriggerException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTriggerException(Throwable cause) {
        super(cause);
    }

    public EventTriggerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
