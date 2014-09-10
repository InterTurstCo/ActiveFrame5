package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

public interface AfterCreateAfterCommitExtentionHandler extends ExtensionPointHandler{
    
    /**
     * Входная точка обработчика точки расширения. Точка расширения может менять переданный доменный объект.
     * @param deletedDomainObject
     */
    void onAfterCreate(DomainObject createdDomainObject);
    
}
