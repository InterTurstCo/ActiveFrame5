package ru.intertrust.cm.core.dao.impl.extension;

import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Служебный EJB сервиса точек расширения. Необходим для того чтобы точки расширения afterCommit вызывались в отдельной транзакции
 * @author larin
 *
 */
public interface AfterCommitExtensionPointService {
    /**
     * Вызов точек расширения после транзакции
     * @param savedDomainObjectsModificationMap
     * @param savedDomainObjects
     * @param createdDomainObjects
     * @param deletedDomainObjects
     * @param changeStatusDomainObjects
     */
    void afterCommit(
            Map<Id, Map<String, FieldModification>> savedDomainObjectsModificationMap,
            List<DomainObject> savedDomainObjects,
            List<DomainObject> createdDomainObjects,
            Map<Id, DomainObject> deletedDomainObjects,
            List<Id> changeStatusDomainObjects);
}
