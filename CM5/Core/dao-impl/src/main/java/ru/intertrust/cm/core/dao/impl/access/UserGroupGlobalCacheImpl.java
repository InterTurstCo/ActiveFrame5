package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Глобальный кеш. Кеширует информацию о пользователях: идентификатор пользователя для данного логина, вхождение
 * пользователя в статические группы Superusers, Administrators. Также кеширует идентификатор группы Superusers. 
 * Нужно сбрасывать этот
 * кеш после изменения логина пользователя (если это будет поддерживаться) и после изменения состава групп Superusers, Administrators.
 * @author atsvetkov
 */
public class UserGroupGlobalCacheImpl implements UserGroupGlobalCache {

//    private Map<String, Id> loginToUserIdCache = new HashMap<String, Id>();
    
    private Map<Id, Boolean> personIdToIsSuperUserCache = new ConcurrentHashMap<Id, Boolean>();
    private Map<Id, Boolean> personIdToIsAdministratorCache = new ConcurrentHashMap<Id, Boolean>();
    private Map<Id, Boolean> personIdToIsInfoSecAuditorCache = new ConcurrentHashMap<Id, Boolean>();
    
    private Id superUsersGroupId = null;

    private Id administratorsGroupId = null;
    
    private Id infoSecAuditorGroupId = null;
    

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
    
    private Id getInfoSecAuditorGroupId() {
        if (infoSecAuditorGroupId == null) {
            infoSecAuditorGroupId = personManagementService.getGroupId(GenericDomainObject.INFO_SEC_AUDITOR_GROUP);
        }
        return infoSecAuditorGroupId;
    }

    private Id getAdministratorsGroupId() {
        if (administratorsGroupId == null) {
            administratorsGroupId = personManagementService.getGroupId(GenericDomainObject.ADMINISTRATORS_STATIC_GROUP);
        }
        return administratorsGroupId;
    }
    
    @Override
    public boolean isPersonSuperUser(Id personId) {

        final Boolean cached = personIdToIsSuperUserCache.get(personId);
        if (cached != null) {
            return cached;
        }
        final boolean isSuperUser = personManagementService.isPersonInGroup(getSuperUsersGroupId(), personId);
        personIdToIsSuperUserCache.put(personId, isSuperUser);
        return isSuperUser;

    }

    @Override
    public boolean isAdministrator(Id personId) {
        final Boolean cached = personIdToIsAdministratorCache.get(personId);
        if (cached != null) {
            return cached;
        }
        final boolean isAdministrator = personManagementService.isPersonInGroup(getAdministratorsGroupId(), personId);
        personIdToIsAdministratorCache.put(personId, isAdministrator);
        return isAdministrator;
    }

    @Override
    public void cleanCache() {
        personIdToIsSuperUserCache.clear();
        personIdToIsAdministratorCache.clear();
        personIdToIsInfoSecAuditorCache.clear();
    }

    @Override
    public boolean isInfoSecAuditor(Id personId) {
        final Boolean cached = personIdToIsInfoSecAuditorCache.get(personId);
        if (cached != null) {
            return cached;
        }
        final boolean isInfoSecAuditor = personManagementService.isPersonInGroup(getInfoSecAuditorGroupId(), personId);
        personIdToIsInfoSecAuditorCache.put(personId, isInfoSecAuditor);
        return isInfoSecAuditor;    
    }

}
