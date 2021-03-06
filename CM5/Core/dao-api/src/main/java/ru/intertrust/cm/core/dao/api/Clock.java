package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.Stamp;

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
