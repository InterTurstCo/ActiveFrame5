package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AuditService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.FieldModificationImpl;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResultImpl;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

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

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private AuditLogServiceDao auditLogServiceDao;

    /**
     * Получение всех версий доменного объекта
     * @param domainObjectId
     * @return
     */
    @Override
    public List<DomainObjectVersion> findAllVersions(Id domainObjectId) {
        try {
            return auditLogServiceDao.findAllVersions(domainObjectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * Получение конкретной версии по известному идентификатору
     * @param versionId
     * @return
     */
    @Override
    public DomainObjectVersion findVersion(Id versionId) {
        try {
            return auditLogServiceDao.findVersion(versionId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * Очистка аудита доменного объекта.
     * @param domainObjectId
     */
    @Override
    public void clean(Id domainObjectId) {
        try {
            auditLogServiceDao.clean(domainObjectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * Получение информации об изменениях между текущей версией доменного
     * объекта и версией с переданным идентификатором
     * @param baseVersionId
     * @return
     */
    @Override
    public VersionComparisonResult compare(Id baseVersionId) {
        try {
            // Получение версий
            DomainObjectVersion baseVersion = findVersion(baseVersionId);
            DomainObjectVersion comparedVersion = auditLogServiceDao.findLastVersion(baseVersion.getDomainObjectId());

            return compare(baseVersion, comparedVersion);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
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
        try {
            // Получение версий
            DomainObjectVersion baseVersion = findVersion(baseVersionId);
            DomainObjectVersion comparedVersion = findVersion(comparedVersionId);

            return compare(baseVersion, comparedVersion);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
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

    @Override
    public DomainObjectVersion findPreviousVersion(Id versionId) {
        return auditLogServiceDao.findPreviousVersion(versionId);
    }

}
