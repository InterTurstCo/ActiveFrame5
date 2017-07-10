package ru.intertrust.cm.core.dao.impl.personmanager;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import java.util.List;

/**
 * Точка расширения после изменения состава статических и динамических групп.
 * @author atsvetkov
 *
 */
@ExtensionPoint(filter = "Group_Member")
public class OnSaveGroupMemberExtensionPoint implements AfterSaveExtensionHandler, AfterDeleteExtensionHandler {

    @Autowired
    private UserGroupGlobalCache userGroupGlobalCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        clearCaches(domainObject.getReference("person_id"), groupChanged(changedFields));

        Id usergroupId = domainObject.getReference("UserGroup");
        if (usergroupId != null) {
            AccessToken accessToken = accessControlService.createSystemAccessToken("OnSaveGroupMemberExtensionPoint");
            DomainObject userGroup = domainObjectDao.find(usergroupId, accessToken);

            String groupName = userGroup.getString("group_name");
            // если изменяется состав группы Superusers, нужно очищать кеш пользователей в AccessControlService
            if (GenericDomainObject.SUPER_USERS_STATIC_GROUP.equals(groupName)) {
                userGroupGlobalCache.cleanCache();
            }
        }
    }

    private void clearCaches(Id personId, boolean clearGlobalCache) {
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP_AND_SUBGROUP.name());
        if (clearGlobalCache) {
            globalCacheClient.notifyPersonGroupChanged(personId);
        }

    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        clearCaches(deletedDomainObject.getReference("person_id"), true);
    }

    private boolean groupChanged(List<FieldModification> changedFields) {
        if (changedFields != null) {
            for (FieldModification fieldModification : changedFields) {
                if (fieldModification.getName().equalsIgnoreCase("UserGroup")) {
                    return true;
                }
            }
        }
        return false;
    }
}
