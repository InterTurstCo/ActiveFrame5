package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс точки расширения, вызывающийся после изменения статуса доменного объекта..
 * @author atsvetkov
 */
public interface AfterChangeStatusExtentionHandler extends ExtensionPointHandler {

    /**
     * Метод, реализующий функционал точки расширения.
     * @param domainObject
     */
    void onAfterChangeStatus(DomainObject domainObject);
}
