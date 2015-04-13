package ru.intertrust.cm.core.dao.impl.access.collector;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.ContextRoleConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

/**
 * Коллектор контекстной роли Владелец аутентификационной информации
 * @author larin
 * 
 */
public class AuthentificationInfoOwnerRoleCollector implements ContextRoleCollector {
    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private AccessControlService accessService;
    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Override
    public List<Id> getMembers(Id contextId) {
        List<Id> result = new ArrayList<Id>();
        //Получаем ид персоны
        String query = "select p.id from person p inner join Authentication_Info i on (i.User_Uid = p.login) where i.id={0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(contextId));
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0,
                accessService.createSystemAccessToken(this.getClass().getName()));
        //Должна найтись только одна запись        
        if (collection.size() > 0) {
            Id personId = collection.get(0).getId();
            //Получаем группу Person для ДО Person
            DomainObject personDynGroup = personManagementService.findDynamicGroup("Person", personId);
            result.add(personDynGroup.getId());
        }
        return result;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add("Person");
        result.add("Authentication_Info");
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        List<Id> result = new ArrayList<Id>();
        if (domainObject.getTypeName().equalsIgnoreCase("Authentication_Info")) {
            //Добавляем непосредственно Authentication_Info
            result.add(domainObject.getId());
        } else {//Person и наследники
            //Получаем Authentication_Info запросом
            String query = "select id from Authentication_Info where User_Uid={0}";
            List<Value> params = new ArrayList<Value>();
            params.add(new StringValue(domainObject.getString("login")));
            IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0,
                    accessService.createSystemAccessToken(this.getClass().getName()));
            //Должна найтись только одна запись
            if (collection.size() > 0) {
                result.add(collection.get(0).getId());
            }
        }
        return result;
    }

    @Override
    public void init(ContextRoleConfig config, CollectorSettings collectorSettings) {
        // Nothing to do        
    }

}
