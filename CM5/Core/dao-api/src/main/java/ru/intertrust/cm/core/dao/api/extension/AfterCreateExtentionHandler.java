package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс обработчика точки расширения вызывающийся после создания доменного объект в конце транзакции
 * @author larin
 *
 */
public interface AfterCreateExtentionHandler extends ExtensionPointHandler{
    
    /**
     * Входная точка обработчика точки расширения. Точка расширения может менять переданный доменный объект.
     * @param deletedDomainObject
     */
    void onAfterCreate(DomainObject createdDomainObject);
    
}
