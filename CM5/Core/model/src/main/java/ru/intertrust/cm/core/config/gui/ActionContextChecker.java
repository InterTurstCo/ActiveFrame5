package ru.intertrust.cm.core.config.gui;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс проверки доступности действия в зависимости от состояния доменного объекта
 * @author larin
 *
 */
public interface ActionContextChecker {
    /**
     * Возвращает true в случае доступности действия
     * @param domainObject
     * @return
     */
    boolean contextAvailable(DomainObject domainObject);
}
