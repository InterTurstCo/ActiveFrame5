package ru.intertrust.cm.globalcacheclient;

import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.globalcacheclient.impl.ClusterCommitStampsInfo;

/**
 * Сервис, обеспечивающий хранение информации о метках времени крайних транзакций на всех узлах кластера
 * @author larin
 *
 */
public interface ClusterTransactionStampService {
    
    /**
     * Получение локальной информации о временных метках транзакций кластера
     * @return
     */
    ClusterCommitStampsInfo getInvalidationCacheInfo();

    /**
     * Установка временной метки транзакции определенного сервера
     * @param serverName
     * @param serverStamp
     */
    void setInvalidationCacheInfo(String serverNodeId, Stamp serverStamp);

    /**
     * Установка временной метки транзакции локального сервера
     * @param serverStamp
     */
    void setLocalInvalidationCacheInfo(Stamp serverStamp);
    
    /**
     * Сброс данных о временных метках
     */
    void resetInvalidationCacheInfo();
}
