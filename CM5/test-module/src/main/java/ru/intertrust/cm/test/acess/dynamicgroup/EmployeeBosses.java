package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

public class EmployeeBosses extends DynamicGroupCollectorBase implements DynamicGroupCollector {

    @Override
    public List<Id> getPersons(Id contextId) {
        String query = "select d.boss from Employee e ";
        query += "inner join Department d on (e.department = d.id) ";
        query += "where d.boss is not null ";
        query += "and e.id = " + ((RdbmsId) contextId).getId();

        return getIdsByQuery(query, "boss");
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());

        List<Id> result = new ArrayList<Id>();
        String query = "select d.organization from Employee e ";
        query += "inner join Department d on (e.department = d.id) ";
        query += "where e.id =" + ((RdbmsId) contextId).getId();
        List<Id> organizationIds = getIdsByQuery(query, "organization");
        if (organizationIds.size() > 0) {
            DomainObject organization = domainObjectDao.find(organizationIds.get(0), accessToken);

            if (organization.getReference("Boss") != null) {
                result.add(personManagementServiceDao.findDynamicGroup("PersonAndDelegates",
                        organization.getReference("Boss")).getId());
            }
        }

        return result;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add("Organization");
        result.add("Department");
        result.add("Employee");
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        List<Id> result = new ArrayList<Id>();
        if (domainObject.getTypeName().equals("Organization") && containsModifiedField(modifiedFields, "Boss")) {
            result.addAll(getIdsByQuery("select e.id from Employee e inner join Department d on (e.department = d.id) where d.organization = "
                    + ((RdbmsId) domainObject.getId()).getId()));
        } else if (domainObject.getTypeName().equals("Department") &&
                (containsModifiedField(modifiedFields, "Boss") ||
                        containsModifiedField(modifiedFields, "ParentDepartment") ||
                containsModifiedField(modifiedFields, "Organization"))) {
            result.addAll(getIdsByQuery("select e.id from Employee e where e.department = "
                    + ((RdbmsId) domainObject.getId()).getId()));
        } else if (domainObject.getTypeName().equals("Employee") &&
                (containsModifiedField(modifiedFields, "Department"))) {
            result.add(domainObject.getId());
        }
        return result;
    }

    @Override
    public void init(DynamicGroupConfig config, CollectorSettings setings) {
    }

}
