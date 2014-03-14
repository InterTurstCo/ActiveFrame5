package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Интерфейс получения списка идентификаторов доменных объектов относительно контекстного доменного объекта
 * @author larin
 *
 */
public interface DomainObjectFinder {
    /**
     * Метод получения списка идентификатолв доменных объектов относительно контекстного доменного объекта
     * @param contextDomainObjectId
     * @return
     */
    List<Id> findObjects(Id contextDomainObjectId);
}
