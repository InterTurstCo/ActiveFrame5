package ru.intertrust.cm.globalcacheclient.cluster;

import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;

/**
 * Сервис работы с JMS сообщениями для нужд глобального кэша
 * @author larin
 *
 */
public interface GlobalCacheJmsHelper {
    
    void sendClusterNotification(CacheInvalidation message);
    
}
