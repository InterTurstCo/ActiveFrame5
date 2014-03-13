package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.ContextRoleConfig;
import ru.intertrust.cm.core.config.StaticGroupCollectorConfig;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;

public class ContextRoleStaticGroupCollector extends BaseDynamicGroupServiceImpl implements ContextRoleCollector {
    private StaticGroupCollectorConfig settings = null;
    private ContextRoleConfig config;

    @Override
    public List<Id> getMembers(Id contextId) {
        List<Id> result = new ArrayList<Id>();
        result.add(personManagementService.getGroupId(settings.getName()));
        return result;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add(config.getContext().getDomainObject().getType());
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        List<Id> result = new ArrayList<Id>();
        result.add(domainObject.getId());
        return result;
    }

    @Override
    public void init(ContextRoleConfig config, CollectorSettings collectorSettings) {
        settings = (StaticGroupCollectorConfig) collectorSettings;
        this.config = config;
    }

}
