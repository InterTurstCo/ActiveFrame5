package ru.intertrust.cm.core.dao.impl.access;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;

public class ContextRoleTrackDomainObjectCollector implements ContextRoleCollector{

    @Override
    public List<Id> getMembers(Id domainObjectId, Id contextId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void init(ContextRoleConfig config) {
        // TODO Auto-generated method stub
        
    }

    public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
        // TODO Auto-generated method stub
        
    }

}
