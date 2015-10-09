package ru.intertrust.cm.core.model;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = false, inherited = true)
public class NonRollingBackException extends SystemException {

    protected NonRollingBackException() {
    }

    protected NonRollingBackException(String message) {
        super(message);
    }

    protected NonRollingBackException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NonRollingBackException(Throwable cause) {
        super(cause);
    }

    protected NonRollingBackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause); // игнорируем 2 остальных параметра в интересах GWT, не воспринимающего методы JDK 1.7
    }
}
