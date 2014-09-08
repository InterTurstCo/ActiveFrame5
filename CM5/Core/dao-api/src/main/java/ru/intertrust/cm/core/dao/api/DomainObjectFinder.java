package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.FindObjectSettings;

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
    
    /**
     * Инициализация дополнительных настроек, которые приходят в класс, реализующий данный интерфейс.
     * Дополнительные настройки указываются внутри тега <find-person-settings> в виде произвольного xml.
     * @param settings дополнительые настройки конфигурации поиска
     * @param extensionContext дополнительые настройки контекста

     */
    void init(FindObjectSettings settings, Dto extensionContext);
}
