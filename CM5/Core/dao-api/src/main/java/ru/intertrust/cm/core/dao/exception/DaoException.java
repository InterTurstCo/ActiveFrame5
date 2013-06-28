package ru.intertrust.cm.core.dao.exception;

import ru.intertrust.cm.core.model.SystemException;

/**
 * Базовый класс для всех исключительных ситуаций DAO слоя
 * @author skashanski
 *
 */
public class DaoException extends SystemException {

    private static final long serialVersionUID = -3516700063113406396L;

    public DaoException() {
        super();
    }

    public DaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }

}
