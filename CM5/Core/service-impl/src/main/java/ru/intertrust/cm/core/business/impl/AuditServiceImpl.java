package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.AuditService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.FieldModificationImpl;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResultImpl;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;

/**
 * Интерфейс сервиса работы с Audit логом
 * @author larin
 * 
 */
@Stateless(name = "AuditService")
@Local(AuditService.class)
@Remote(AuditService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogServiceDao auditLogServiceDao;

    /**
     * Получение всех версий доменного объекта
     * @param domainObjectId
     * @return
     */
    @Override
    public List<DomainObjectVersion> findAllVersions(Id domainObjectId) {
        return auditLogServiceDao.findAllVersions(domainObjectId);
    }

    /**
     * Получение конкретной версии по известному идентификатору
     * @param versionId
     * @return
     */
    @Override
    public DomainObjectVersion findVersion(Id versionId) {
        return auditLogServiceDao.findVersion(versionId);
    }

    /**
     * Очистка аудита доменного объекта.
     * @param domainObjectId
     */
    @Override
    public void clean(Id domainObjectId) {
        auditLogServiceDao.clean(domainObjectId);
    }

    /**
     * Получение информации об изменениях между текущей версией доменного
     * объекта и версией с переданным идентификатором
     * @param baseVersionId
     * @return
     */
    @Override
    public VersionComparisonResult compare(Id baseVersionId) {
        // Получение версий
        DomainObjectVersion baseVersion = findVersion(baseVersionId);
        DomainObjectVersion comparedVersion = auditLogServiceDao.findLastVersion(baseVersion.getDomainObjectId());

        return compare(baseVersion, comparedVersion);
    }

    /**
     * Получение информации об изменениях в двух разных версиях доменного
     * объекта по известным идентификаторам версий
     * @param baseVersionId
     * @param comparedVersionId
     * @return
     */
    @Override
    public VersionComparisonResult compare(Id baseVersionId, Id comparedVersionId) {
        // Получение версий
        DomainObjectVersion baseVersion = findVersion(baseVersionId);
        DomainObjectVersion comparedVersion = findVersion(comparedVersionId);

        return compare(baseVersion, comparedVersion);
    }

    /**
     * Формирования результата сравнений двух версий доменного объекта
     * @param baseVersion
     * @param comparedVersion
     * @return
     */
    private VersionComparisonResult compare(DomainObjectVersion baseVersion, DomainObjectVersion comparedVersion) {

        // Формирование результата
        VersionComparisonResultImpl result = new VersionComparisonResultImpl();
        result.setBaseVersionId(baseVersion.getId());
        result.setComparedVersionId(comparedVersion.getId());
        result.setComponent(comparedVersion.getComponent());
        result.setDomainObjectId(baseVersion.getDomainObjectId());
        result.setIpAddress(comparedVersion.getIpAddress());
        result.setModifiedDate(comparedVersion.getModifiedDate());
        result.setModifier(comparedVersion.getModifier());
        result.setVersionInfo(comparedVersion.getVersionInfo());

        for (String fieldName : baseVersion.getFields()) {
            Value baseValue = baseVersion.getValue(fieldName);
            Value comparedValue = comparedVersion.getValue(fieldName);
            if (baseValue.get() != null ? !baseValue.get().equals(comparedValue.get()) : comparedValue.get() != null) {
                result.addFieldModification(new FieldModificationImpl(fieldName, baseValue, comparedValue));
            }
        }

        return result;
    }

}
