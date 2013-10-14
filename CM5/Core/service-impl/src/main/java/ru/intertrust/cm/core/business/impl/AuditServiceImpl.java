package ru.intertrust.cm.core.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AuditService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;

/**
 * Интерфейс сервиса работы с Audit логом
 * @author larin
 *
 */
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogServiceDao auditLogServiceDao;
    
    /**
     * Получение всех версий доменного объекта
     * @param domainObjectId
     * @return
     */
    @Override
    public List<DomainObjectVersion> findAllVersions(Id domainObjectId){
        return auditLogServiceDao.findAllVersions(domainObjectId);
    }
    
    /**
     * Получение конкретной версии по известному идентификатору
     * @param versionId
     * @return
     */
    @Override
    public DomainObjectVersion findVersion(Id versionId){
        return auditLogServiceDao.findVersion(versionId);
    }
    
    /**
     * Очистка аудита доменного объекта.
     * @param domainObjectId
     */
    @Override
    public void clean(Id domainObjectId){
        auditLogServiceDao.clean(domainObjectId);
    }
    
    /**
     * Получение информации об изменениях между текущей версией доменного объекта и версией с переданным идентификатором
     * @param baseVersionId
     * @return
     */
    @Override
    public VersionComparisonResult compare(Id baseVersionId){
        return null;
    }
    
    /**
     * Получение информации об изменениях в двух разных версиях доменного объекта по известным идентификаторам версий
     * @param baseVersionId
     * @param comparedVersionId
     * @return
     */
    @Override
    public VersionComparisonResult compare(Id baseVersionId, Id comparedVersionId){
        return null;
    }

}
