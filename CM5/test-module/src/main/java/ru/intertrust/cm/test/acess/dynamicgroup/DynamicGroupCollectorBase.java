package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

public class DynamicGroupCollectorBase {

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected CollectionsDao collections;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected PersonManagementServiceDao personManagementServiceDao;

    /**
     * Получение коллекций идентификаторов по переданному запросу
     * 
     * @param string
     * @return
     */
    protected List<Id> getIdsByQuery(String query) {
        return getIdsByQuery(query, null);
    }

    protected List<Id> getIdsByQuery(String query, String fieldName) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collections.findCollectionByQuery(query, 0, 0, accessToken);
        List<Id> result = new ArrayList<Id>();
        for (IdentifiableObject identifiableObject : collection) {
            if (fieldName == null) {
                result.add(identifiableObject.getId());
            } else {
                result.add(identifiableObject.getReference(fieldName));
            }
        }
        return result;
    }

    protected Id getIdByQuery(String query) {
        List<Id> result = getIdsByQuery(query);
        if (result.size() > 0) {
            return result.get(0);
        } else{
            return null;
        }
    }

    protected boolean containsModifiedField(
            List<FieldModification> modifiedFieldNames, String fieldName) {
        if (modifiedFieldNames != null) {
            for (FieldModification fieldModification : modifiedFieldNames) {
                if (fieldModification.getName().equalsIgnoreCase(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
