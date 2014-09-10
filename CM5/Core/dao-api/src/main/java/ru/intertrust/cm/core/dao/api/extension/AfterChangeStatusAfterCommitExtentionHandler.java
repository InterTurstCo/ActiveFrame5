package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс обработчика точки расширения, вызывающийся после изменения статуса доменного объекта. после комита транзакции.
 * Если в течение одной транзакции доменный объект несколько раз поменяет статус то данный обработчик вызовется только один раз
 * @author atsvetkov
 */
public interface AfterChangeStatusAfterCommitExtentionHandler extends ExtensionPointHandler {

    /**
     * Метод, реализующий функционал точки расширения.
     * @param domainObject
     */
    void onAfterChangeStatus(DomainObject domainObject);
}
