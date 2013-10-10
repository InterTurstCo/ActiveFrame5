package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;

/**
 * Интерфейс сервиса работы с Audit логом
 * @author larin
 *
 */
public interface AuditLogServiceDao {

    /**
     * Получение всех версий доменного объекта
     * @param domainObjectId
     * @return
     */
    List<DomainObjectVersion> getAllVersions(Id domainObjectId);
    
    /**
     * Получение конкретной версии по известному идентификатору
     * @param versionId
     * @return
     */
    DomainObjectVersion getVersion(Id versionId);
    
    /**
     * Очистка аудита доменного объекта. Доступен только в спринг бине внутреннего API
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

}
