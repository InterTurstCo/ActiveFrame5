package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис работы со статусами. Предоставляет методы поиска статуса по идентификатору и поиска идентификатора статуса по
 * названию. Названия статусов и их идентификаторы кешируются в глобальном кеше.
 * @author atsvetkov
 */
public interface StatusDao {

    /**
     * Поиск идентификатора статуса по названию.
     * @param statusName название статуса
     * @return
     */
    Id getStatusIdByName(String statusName);

    /**
     * Поиск названия статуса по идентификатору.
     * @param statusId
     * @return
     */
    String getStatusNameById(Id statusId);
    
    /**
     * Очитска глобального кеша статусов.
     */
    void resetCache();
    
}
