package ru.intertrust.cm.core.dao.impl.access;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

/**
 * Глобальный кеш. Кеширует информацию о пользователях: идентификатор пользователя для данного логина, вхождение
 * пользователя в статические группы Superusers, Administrators. Также кеширует идентификатор группы Superusers. 
 * Нужно сбрасывать этот
 * кеш после изменения логина пользователя (если это будет поддерживаться) и после изменения состава групп Superusers, Administrators.
 * @author atsvetkov
 */
public class UserGroupGlobalCacheImpl implements UserGroupGlobalCache {

//    private Map<String, Id> loginToUserIdCache = new HashMap<String, Id>();
    
    private Map<Id, Boolean> personIdToIsSuperUserCache = new HashMap<Id, Boolean>();
    private Map<Id, Boolean> personIdToIsAdministratorCache = new HashMap<Id, Boolean>();
    
    private Id superUsersGroupId = null;

    private Id administratorsGroupId = null;

    @Autowired    
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private PersonServiceDao personService;

    public void setPersonManagementService(PersonManagementServiceDao personManagementService) {
        this.personManagementService = personManagementService;
    }

    @Override
    public Id getUserIdByLogin(String login) {
        return personService.findPersonByLogin(login).getId();
    }

    private Id getSuperUsersGroupId() {
        if (superUsersGroupId == null) {
            superUsersGroupId = personManagementService.getGroupId(GenericDomainObject.SUPER_USERS_STATIC_GROUP);
        }
        return superUsersGroupId;
    }

    private Id getAdministratorsGroupId() {
        if (administratorsGroupId == null) {
            administratorsGroupId = personManagementService.getGroupId(GenericDomainObject.ADMINISTRATORS_STATIC_GROUP);
        }
        return administratorsGroupId;
    }
    
    @Override
    public boolean isPersonSuperUser(Id personId) {

        boolean isSuperUser = false;
        if (personIdToIsSuperUserCache.get(personId) != null) {
            isSuperUser = personIdToIsSuperUserCache.get(personId);
        } else {
            isSuperUser = personManagementService.isPersonInGroup(getSuperUsersGroupId(), personId);
            personIdToIsSuperUserCache.put(personId, isSuperUser);
        }
        return isSuperUser;

    }

    @Override
    public boolean isAdministrator(Id personId) {

        boolean isAdministrator = false;
        if (personIdToIsAdministratorCache.get(personId) != null) {
            isAdministrator = personIdToIsAdministratorCache.get(personId);
        } else {
            isAdministrator = personManagementService.isPersonInGroup(getAdministratorsGroupId(), personId);
            personIdToIsAdministratorCache.put(personId, isAdministrator);
        }
        return isAdministrator;
    }

    @Override
    public void cleanCache() {
        personIdToIsSuperUserCache.clear();
        personIdToIsAdministratorCache.clear();
    }

}
