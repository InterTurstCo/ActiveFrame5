package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.UPDATED_DATE_COLUMN;

public class BasicVersionRowMapper extends BasicRowMapper {

    public BasicVersionRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
                          DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, idField, configurationExplorer, domainObjectTypeIdCache);
    }

    protected DomainObjectVersion buildDomainObjectVersion(ResultSet rs, ColumnModel columnModel) throws SQLException {
        GenericDomainObjectVersion object = new GenericDomainObjectVersion(buildDomainObject(rs, columnModel));

        int typeId = domainObjectTypeIdCache.getId(domainObjectType);

        // Установка полей версии
        object.setId(new RdbmsId(typeId, rs.getLong(DomainObjectDao.ID_COLUMN)));
        object.setDomainObjectId(new RdbmsId(typeId, rs.getLong(DomainObjectDao.DOMAIN_OBJECT_ID_COLUMN)));
        object.setModifiedDate(rs.getTimestamp(UPDATED_DATE_COLUMN));

        ReferenceValue updatedByRef = readReferenceValue(rs, DomainObjectDao.UPDATED_BY);
        object.setModifier(updatedByRef.get());

        object.setVersionInfo(rs.getString(DomainObjectDao.INFO_COLUMN));
        object.setIpAddress(rs.getString(DomainObjectDao.IP_ADDRESS_COLUMN));
        object.setComponent(rs.getString(DomainObjectDao.COMPONENT_COLUMN));
        object.setOperation(getOperation(rs.getInt(DomainObjectDao.OPERATION_COLUMN)));

        object.resetDirty();

        return object;
    }

    private DomainObjectVersion.AuditLogOperation getOperation(int operation){
        DomainObjectVersion.AuditLogOperation result = null;
        if (operation == 1){
            result = DomainObjectVersion.AuditLogOperation.CREATE;
        }else if(operation == 2){
            result = DomainObjectVersion.AuditLogOperation.UPDATE;
        }else{
            result = DomainObjectVersion.AuditLogOperation.DELETE;
        }
        return result;
    }

}
