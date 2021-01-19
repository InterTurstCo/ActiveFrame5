package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Точка расширения на удаление доменного объекта. Вызывается после удаления.
 * Вызывается в той же транзакции что и удаление
 */
public interface AfterDeleteExtensionHandler extends ExtensionPointHandler{
    /**
     * Входная точка обработчика точки расширения удаления доменного объекта.
     * 
     * @param deletedDomainObject
     */
    void onAfterDelete(DomainObject deletedDomainObject);
}
