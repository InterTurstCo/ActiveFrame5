package ru.intertrust.cm.core.dao.api;

/**
 * Сервис меток времени
 * @author larin
 *
 */
public interface Clock {
    
    /**
     * Получение следующей метки времени
     * @return
     */
    Stamp<?> nextStamp();
}
