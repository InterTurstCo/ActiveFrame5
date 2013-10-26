package ru.intertrust.cm.core.dao.impl.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

public class TrackDomainObjectCollector implements DynamicGroupCollector {

    private DynamicGroupConfig config;
    private TrackDomainObjectsConfig settings;

    @Override
    public List<Id> getPersons(Id contextId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Id> getInvalidDynamicGroups(DomainObject domainObject,
            List<FieldModification> modifiedFields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void init(DynamicGroupConfig config) {
        this.config = config;
        this.settings = config.getMembers().getTrackDomainObjects();
    }

}
