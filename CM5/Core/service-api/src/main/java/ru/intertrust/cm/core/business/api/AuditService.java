package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;

/**
 * Интерфейс сервиса работы с Audit логом
 * @author larin
 *
 */
public interface AuditService {

    /**
     * Удаленный интерфейс
     * @author larin
     *
     */
    public interface Remote extends AuditService {
    }
    
    /**
     * Получение всех версий доменного объекта
     * @param domainObjectId
     * @return
     */
    List<DomainObjectVersion> findAllVersions(Id domainObjectId);
    
    /**
     * Получение конкретной версии по известному идентификатору
     * @param versionId
     * @return
     */
    DomainObjectVersion findVersion(Id versionId);
    
    /**
     * Очистка аудита доменного объекта.
     * @param domainObjectId
     */
    void clean(Id domainObjectId);
    
    /**
     * Получение информации об изменениях между текущей версией доменного объекта и версией с переданным идентификатором
     * @param baseVersionId
     * @return
     */
    VersionComparisonResult compare(Id baseVersionId);
    
    /**
     * Получение информации об изменениях в двух разных версиях доменного объекта по известным идентификаторам версий
     * @param baseVersionId
     * @param comparedVersionId
     * @return
     */
    VersionComparisonResult compare(Id baseVersionId, Id comparedVersionId);

    /**
     * Получение предыдущей версии
     * @param versionId
     * @return
     */
    DomainObjectVersion findPreviousVersion(Id versionId);
}
