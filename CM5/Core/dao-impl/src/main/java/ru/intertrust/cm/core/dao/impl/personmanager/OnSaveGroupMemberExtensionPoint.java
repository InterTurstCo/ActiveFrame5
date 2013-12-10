package ru.intertrust.cm.core.dao.impl.personmanager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Точка расширения после изменения состава статических и динамических групп.
 * @author atsvetkov
 *
 */
@ExtensionPoint(filter = "Group_Member")
public class OnSaveGroupMemberExtensionPoint implements AfterSaveExtensionHandler {

    @Autowired
    private UserGroupGlobalCache userGroupGlobalCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        Id usergroupId = domainObject.getReference("UserGroup");
        if (usergroupId != null) {
            AccessToken accessToken = accessControlService.createSystemAccessToken("OnSaveGroupMemberExtensionPoint");
            DomainObject userGroup = domainObjectDao.find(usergroupId, accessToken);

            String groupName = userGroup.getString("group_name");
            // если изменяется состав группы Superusers, нужно очищать кеш в пользователей в AccessControlService
            if (GenericDomainObject.SUPER_USERS_STATIC_GROUP.equals(groupName)) {
                userGroupGlobalCache.cleanCache();
            }
        }
    }

}
