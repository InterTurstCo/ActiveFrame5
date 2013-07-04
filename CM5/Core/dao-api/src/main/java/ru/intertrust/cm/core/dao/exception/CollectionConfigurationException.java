package ru.intertrust.cm.core.dao.exception;

/**
 * Выбрасывется, когда конфигурация коллекции не соотвестсвует списку переданных параметров. Например, когда не все
 * placeholders в прототипе запроса были заполнены, используя переданные параметры.
 * @author atsvetkov
 */
public class CollectionConfigurationException extends DaoException{

    public CollectionConfigurationException() {
        super();
    }

    public CollectionConfigurationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CollectionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionConfigurationException(String message) {
        super(message);
    }

    public CollectionConfigurationException(Throwable cause) {
        super(cause);
    }    
    
}
