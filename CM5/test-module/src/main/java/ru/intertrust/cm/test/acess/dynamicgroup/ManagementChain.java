package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

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
    public List<Id> getPersons(Id domainObjectId, Id contextId) {
        List<Id> result = new ArrayList<Id>();
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        Id rootDepartmentId = getRootDepartment(contextId);
        DomainObject rootDepartment = domainObjectDao.find(rootDepartmentId, accessToken);
        if (rootDepartment.getReference("Boss") != null) {
            result.add(rootDepartment.getReference("Boss"));
        }
        result.addAll(getChildDepartmentBosses(rootDepartmentId));
        return result;
    }

    @Override
    public List<Id> getGroups(Id domainObjectId, Id contextId) {
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
            if (fieldModification.getBaseValue() != null){
                result.addAll(getDepartmentChain(((ReferenceValue) fieldModification.getBaseValue()).get()));
            }
            if (fieldModification.getComparedValue() != null){
                result.addAll(getDepartmentChain(((ReferenceValue) fieldModification.getComparedValue()).get()));
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

        DomainObject department = domainObjectDao.find(departmentId, accessToken);
        Id parentDepartmentId = department.getId();
        while (department != null) {
            if (department.getReference("ParentDepartment") != null) {
                parentDepartmentId = department.getReference("ParentDepartment");
                department = domainObjectDao.find(parentDepartmentId, accessToken);
            } else {
                department = null;
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
                if (fieldModification.getName().equals(fieldName)) {
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
            result.add(department.getReference("Boss"));
            result.addAll(getChildDepartmentBosses(id));
        }
        return result;
    }

    private List<Id> getChildDepartments(Id parentDepartmentId) {
        String query = "select id from ";
        query += "department d ";
        query += "where d.parentdepartment = " + ((RdbmsId) parentDepartmentId).getId();
        List<Id> result = getIdsByQuery(query);

        return result;

    }
}
