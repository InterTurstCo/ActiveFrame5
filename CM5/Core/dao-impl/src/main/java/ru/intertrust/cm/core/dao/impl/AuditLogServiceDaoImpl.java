package ru.intertrust.cm.core.dao.impl;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;

public class AuditLogServiceDaoImpl implements AuditLogServiceDao{

    @Override
    public List<DomainObjectVersion> findAllVersions(Id domainObjectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DomainObjectVersion findVersion(Id versionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clean(Id domainObjectId) {
        // TODO Auto-generated method stub
        
    }

}
