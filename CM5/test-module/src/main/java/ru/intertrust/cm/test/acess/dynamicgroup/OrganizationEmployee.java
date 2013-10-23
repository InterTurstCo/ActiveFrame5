package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

public class OrganizationEmployee implements DynamicGroupCollector {

    private TestDynGroupCollectorSettings settings;

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
    public List<Id> getInvalidDynamicGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void init(CollectorSettings settings) {
        // TODO Auto-generated method stub
        //this.settings = (TestDynGroupCollectorSettings)settings;
    }

}
