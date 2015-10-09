package ru.intertrust.cm.core.model;

/**
 * Клас исключения, формирующегося в подсистеме серверных компонентов.
 * @author atsvetkov
 */
public class ServerComponentException extends NonRollingBackException {

    private static final long serialVersionUID = 1L;

    public ServerComponentException() {
    }

    public ServerComponentException(String message) {
        super(message);
    }

    public ServerComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerComponentException(Throwable cause) {
        super(cause);
    }

    public ServerComponentException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
