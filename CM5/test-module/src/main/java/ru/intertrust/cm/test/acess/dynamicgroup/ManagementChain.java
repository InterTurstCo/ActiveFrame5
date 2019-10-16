package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

/**
 * Реализатора коллектора все цепочка руководства подразделения
 *
 * @author larin
 *
 */
public class ManagementChain extends DynamicGroupCollectorBase implements DynamicGroupCollector {

    private TestDynGroupCollectorSettings settings;
    private DynamicGroupConfig config;

    @Override
    public List<Id> getPersons(Id contextId) {
        List<Id> result = new ArrayList<Id>();
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        Id rootDepartmentId = getRootDepartment(contextId);
        if (rootDepartmentId != null) {
            DomainObject rootDepartment = domainObjectDao.find(rootDepartmentId, accessToken);
            if (rootDepartment.getReference("Boss") != null) {
                result.add(rootDepartment.getReference("Boss"));
            }
            result.addAll(getChildDepartmentBosses(rootDepartmentId));
        }
        return result;
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add("Department");
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject,
            List<FieldModification> modifiedFieldNames) {
        List<Id> result = new ArrayList<Id>();
        if (containsModifiedField(modifiedFieldNames, "ParentDepartment")) {
            FieldModification fieldModification = getFieldModification(modifiedFieldNames, "ParentDepartment");
            if (fieldModification != null) {
                if (fieldModification.getBaseValue() != null) {
                    result.addAll(getDepartmentChain(((ReferenceValue) fieldModification.getBaseValue()).get()));
                }
                if (fieldModification.getComparedValue() != null) {
                    result.addAll(getDepartmentChain(((ReferenceValue) fieldModification.getComparedValue()).get()));
                }
            }
        } else if (containsModifiedField(modifiedFieldNames, "Boss")) {
            result.addAll(getDepartmentChain(domainObject.getId()));
        }

        return result;
    }

    private List<Id> getDepartmentChain(Id departmentId) {
        List<Id> result = new ArrayList<Id>();
        if (departmentId != null) {
            Id rootDepartmentId = getRootDepartment(departmentId);
            result.add(rootDepartmentId);
            result.addAll(getChildDepartments(rootDepartmentId));
        }
        return result;
    }

    private Id getRootDepartment(Id departmentId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());

        Id parentDepartmentId = null;
        DomainObject department = domainObjectDao.find(departmentId, accessToken);
        if (department != null) {
            parentDepartmentId = department.getId();
            while (department != null) {
                if (department.getReference("ParentDepartment") != null) {
                    parentDepartmentId = department.getReference("ParentDepartment");
                    department = domainObjectDao.find(parentDepartmentId, accessToken);
                } else {
                    department = null;
                }
            }
        }
        return parentDepartmentId;
    }

    @Override
    public void init(DynamicGroupConfig config, CollectorSettings settings) {
        this.config = config;
        this.settings = (TestDynGroupCollectorSettings) settings;
    }

    private FieldModification getFieldModification(
            List<FieldModification> modifiedFieldNames, String fieldName) {
        if (modifiedFieldNames != null) {
            for (FieldModification fieldModification : modifiedFieldNames) {
                if (fieldModification.getName().equalsIgnoreCase(fieldName)) {
                    return fieldModification;
                }
            }
        }
        return null;
    }

    private List<Id> getChildDepartmentBosses(Id parentDepartmentId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());

        List<Id> result = new ArrayList<Id>();
        List<Id> childDepartments = getChildDepartments(parentDepartmentId);
        for (Id id : childDepartments) {
            DomainObject department = domainObjectDao.find(id, accessToken);
            if (department.getReference("Boss") != null) {
                result.add(department.getReference("Boss"));
            }
            result.addAll(getChildDepartmentBosses(id));
        }
        return result;
    }

    private List<Id> getChildDepartments(Id parentDepartmentId) {
        String query = "select d.id from ";
        query += "Department d ";
        query += "where d.parentdepartment = " + ((RdbmsId) parentDepartmentId).getId();
        List<Id> childIds = getIdsByQuery(query);
        List<Id> result = new ArrayList<>();
        result.addAll(childIds);
        for (Id id : childIds) {
            result.addAll(getChildDepartments(id));
        }

        return result;
    }
}
