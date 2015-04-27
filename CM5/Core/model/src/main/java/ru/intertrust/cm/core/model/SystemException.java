package ru.intertrust.cm.core.model;

import javax.ejb.ApplicationException;

/**
 * @author vmatsukevich
 *         Date: 6/28/13
 *         Time: 10:46 AM
 */
@ApplicationException(rollback = false, inherited = true)
public abstract class SystemException extends RuntimeException {

    protected SystemException() {
    }

    protected SystemException(String message) {
        super(message);
    }

    protected SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    protected SystemException(Throwable cause) {
        super(cause);
    }

    protected SystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause); // игнорируем 2 остальных параметра в интересах GWT, не воспринимающего методы JDK 1.7
    }
}
