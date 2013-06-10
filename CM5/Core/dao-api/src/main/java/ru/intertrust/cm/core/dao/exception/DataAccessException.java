package ru.intertrust.cm.core.dao.exception;

/**
 * Базовый класс для всех исключительных ситуаций DAO слоя
 * @author skashanski
 *
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = -3516700063113406396L;

    public DataAccessException() {
        super();
    }

    public DataAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }




}
