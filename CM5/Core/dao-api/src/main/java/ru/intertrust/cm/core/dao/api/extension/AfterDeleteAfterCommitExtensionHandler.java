package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс обработчика точки расширения вызывающийся после удаления доменного объекта в конце транзакции
 * @author larin
 *
 */
public interface AfterDeleteAfterCommitExtensionHandler extends ExtensionPointHandler{
    /**
     * Входная точка обработчика точки расширения.
     * 
     * @param domainObject
     */
    void onAfterDelete(DomainObject deletedDomainObject);
}
