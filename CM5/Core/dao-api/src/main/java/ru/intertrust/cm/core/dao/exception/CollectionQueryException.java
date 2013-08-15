package ru.intertrust.cm.core.dao.exception;

/**
 * Выбрасывается, когда запрос коллекции явялется невалидным.
 * @author atsvetkov
 *
 */
public class CollectionQueryException extends DaoException {

    public CollectionQueryException() {
        super();
    }

    public CollectionQueryException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CollectionQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionQueryException(String message) {
        super(message);
    }

    public CollectionQueryException(Throwable cause) {
        super(cause);
    }    
}
