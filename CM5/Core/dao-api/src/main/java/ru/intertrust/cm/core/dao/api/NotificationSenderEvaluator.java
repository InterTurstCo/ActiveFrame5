package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.FindObjectsConfig;

/**
 * Сервис поиска отправителя уведомления.
 * 
 * @author atsvetkov
 *
 */
public interface NotificationSenderEvaluator {
    /**
     * Поиск отправителей. Если в результате вычисления отправителя находится более одной персоны, то возвращается первая из списка!
     * @param getObjectsConfig
     * @param contextDomainObjectId
     * @return
     */
    Id findSender(FindObjectsConfig getObjectsConfig, Id contextDomainObjectId);

}
