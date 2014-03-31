package ru.intertrust.cm.test.acess.contextrole;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.ContextRoleConfig;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;

public class AllUsers implements ContextRoleCollector{

    @Override
    public List<Id> getMembers(Id contextId) {
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
    public void init(ContextRoleConfig config, CollectorSettings collectorSettings) {
        // TODO Auto-generated method stub
        
    }

}
