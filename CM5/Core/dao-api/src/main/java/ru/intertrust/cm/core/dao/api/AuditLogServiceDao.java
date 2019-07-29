package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;

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
     * Получение крайней версии доменного объекта
     * @return
     */
    DomainObjectVersion findLastVersion(Id domainObjectId);

    /**
     * Получение предыдущей версии доменного объекта
     * @param versionId
     * @return
     */
    DomainObjectVersion findPreviousVersion(Id versionId);

}
