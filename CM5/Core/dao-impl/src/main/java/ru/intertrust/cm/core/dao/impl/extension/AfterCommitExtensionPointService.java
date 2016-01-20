package ru.intertrust.cm.core.dao.impl.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;

/**
 * Служебный EJB сервиса точек расширения. Необходим для того чтобы точки расширения afterCommit вызывались в отдельной транзакции
 * @author larin
 *
 */
public interface AfterCommitExtensionPointService {
    /**
     * Вызов точек расширения после транзакции
     * @param domainObjectsModification
     */
    void afterCommit(DomainObjectsModification domainObjectsModification);

    void performAfterCommit(DomainObjectsModification domainObjectsModification);
}
