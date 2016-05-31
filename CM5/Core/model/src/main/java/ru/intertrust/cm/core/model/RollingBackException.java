package ru.intertrust.cm.core.model;

// НЕЛЬЗЯ ВЫНОСИТЬ в import javax.ejb - GWT Dev Mode на проектах, использующих платформу перестаёт запускаться
@javax.ejb.ApplicationException(rollback = true, inherited = true)
public class RollingBackException extends SystemException {

    protected RollingBackException() {
    }

    protected RollingBackException(String message) {
        super(message);
    }

    protected RollingBackException(String message, Throwable cause) {
        super(message, cause);
    }

    protected RollingBackException(Throwable cause) {
        super(cause);
    }

    protected RollingBackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
