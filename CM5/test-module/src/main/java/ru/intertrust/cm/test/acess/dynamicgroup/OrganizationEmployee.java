package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.api.CollectionsDao;

/**
 * Реализатора коллектора все сотрудники организации
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
        List<String> result = new ArrayList<String>();
        result.add("Employee");
        result.add("Department");
        return result;
    }

    @Override
    public List<Id> getInvalidDynamicGroups(DomainObject domainObject, List<String> modifiedFieldNames) {
        List<Id> result = null;
        // Если подразделение и сменилась ссылка на организацию то получаем
        // группу у организации
        if (domainObject.getTypeName().equals("Department") && (modifiedFieldNames == null || modifiedFieldNames.contains("Organization"))) {
            Id organizationId = domainObject.getReference("Organization");

            String query = "select ug.id from ";
            query += "user_group ug on ";
            query += "where ug.object_id = " + ((RdbmsId) organizationId).getId() + " and ug.group_name = '" + config.getName() + "'";

            result = getIdsByQuery(query);
        }else if (domainObject.getTypeName().equals("Employee") && (modifiedFieldNames == null || modifiedFieldNames.contains("Department"))){
            Id organizationId = domainObject.getReference("Department");

            String query = "select ug.id from ";
            query += "user_group ug on ";
            query += "where ug.object_id = " + ((RdbmsId) organizationId).getId() + " and ug.group_name = '" + config.getName() + "'";

            result = getIdsByQuery(query);
            
        }
        return result;
    }

    /**
     * Получение коллекций идентификаторов по переданному запросу
     * @param string
     * @return
     */
    private List<Id> getIdsByQuery(String query) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collections.findCollectionByQuery(query, 0, 0, accessToken);
        List<Id> result = new ArrayList<Id>();
        for (IdentifiableObject identifiableObject : collection) {
            result.add(identifiableObject.getId());
        }
        return result;
    }

    @Override
    public void init(DynamicGroupConfig config) {
        this.config = config;
        this.settings = (TestDynGroupCollectorSettings) config.getMembers().getCollector().getSettings();
    }

}
