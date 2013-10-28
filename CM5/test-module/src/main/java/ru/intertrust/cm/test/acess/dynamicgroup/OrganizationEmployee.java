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
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.api.CollectionsDao;

/**
 * Реализатора коллектора все сотрудники организации
 * 
 * @author larin
 * 
 */
public class OrganizationEmployee implements DynamicGroupCollector {

    private TestDynGroupCollectorSettings settings;
    private DynamicGroupConfig config;

    @Autowired
    private CollectionsDao collections;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public List<Id> getPersons(Id domainObjectId, Id contextId) {
        String query = "select e.id from ";
        query += "employee e ";
        query += "department d on (e.department = d.id) ";
        query += "where d.organizationid = " + ((RdbmsId) contextId).getId();
        List<Id> result = getIdsByQuery(query);

        return result;
    }

    @Override
    public List<Id> getGroups(Id domainObjectId, Id contextId) {
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add("Employee");
        result.add("Department");
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject,
            List<FieldModification> modifiedFieldNames) {
        List<Id> result = new ArrayList<Id>();
        // Если изменилось подразделение и сменилась ссылка на организацию то
        // получаем
        // группу у организации
        if (domainObject.getTypeName().equals("Department")
                && containsModifiedField(modifiedFieldNames, "Organization")) {

            FieldModification fieldModification = getFieldModification(modifiedFieldNames, "Organization");


            if (fieldModification.getBaseValue() != null && fieldModification.getComparedValue() != null) {
                // В случае изменения
                result.add((Id)fieldModification.getBaseValue().get());
                result.add((Id)fieldModification.getComparedValue().get());
            } else if (fieldModification.getBaseValue() == null && fieldModification.getComparedValue() != null) {
                // В случае создания
                result.add((Id)fieldModification.getComparedValue().get());
            } else if (fieldModification.getBaseValue() != null && fieldModification.getComparedValue() == null) {
                // В случае удаления
                result.add((Id)fieldModification.getBaseValue().get());
            }
        } else if (domainObject.getTypeName().equals("Employee")
                && (modifiedFieldNames == null || containsModifiedField(
                        modifiedFieldNames, "Department"))) {
            // Если изменилась ссылка на подразделение у сотрудника
            FieldModification fieldModification = getFieldModification(modifiedFieldNames, "Department");

            String query = "select d.organisation from department d ";
            query += "where d.id in ( ";

            if (fieldModification.getBaseValue() != null && fieldModification.getComparedValue() != null) {
                // В случае изменения
                query +=
                        ((RdbmsId) ((ReferenceValue) fieldModification.getBaseValue()).get()).getId() + ", "
                                + ((RdbmsId) ((ReferenceValue) fieldModification.getComparedValue()).get()).getId();
            } else if (fieldModification.getBaseValue() == null && fieldModification.getComparedValue() != null) {
                // В случае создания
                query += ((RdbmsId) ((ReferenceValue) fieldModification.getComparedValue()).get()).getId();
            } else if (fieldModification.getBaseValue() != null && fieldModification.getComparedValue() == null) {
                // В случае удаления
                query += ((RdbmsId) ((ReferenceValue) fieldModification.getBaseValue()).get()).getId();
            }

            query += " ) and ug.group_name = '" + config.getName() + "'";

            result = getIdsByQuery(query);

        }
        return result;
    }

    /**
     * Получение коллекций идентификаторов по переданному запросу
     * 
     * @param string
     * @return
     */
    private List<Id> getIdsByQuery(String query) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collections
                .findCollectionByQuery(query, 0, 0, accessToken);
        List<Id> result = new ArrayList<Id>();
        for (IdentifiableObject identifiableObject : collection) {
            result.add(identifiableObject.getId());
        }
        return result;
    }

    @Override
    public void init(DynamicGroupConfig config) {
        this.config = config;
        this.settings = (TestDynGroupCollectorSettings) config.getMembers()
                .getCollector().getSettings();
    }

    private boolean containsModifiedField(
            List<FieldModification> modifiedFieldNames, String fieldName) {
        if (modifiedFieldNames != null) {
            for (FieldModification fieldModification : modifiedFieldNames) {
                if (fieldModification.getName().equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
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
}
