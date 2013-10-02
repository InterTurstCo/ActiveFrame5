package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

public interface AfterDeleteExtensionHandler extends ExtensionPointHandler{
    /**
     * Входная точка обработчика точки расширения.
     * 
     * @param domainObject
     */
    void onAfterDelete(DomainObject deletedDomainObject);
}
