package ru.intertrust.cm.test.extension;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.model.FatalException;

@ExtensionPoint(filter = "country")
public class AfterSaveCountry implements AfterSaveExtensionHandler {
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private PermissionServiceDao permissionService;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    protected UserGroupGlobalCache userGroupGlobalCache;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        // Проверяем что объект текущий виден в этой же транзакции запросом для проверки CMFIVE-1779
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(domainObject.getId()));
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select * from country where id = {0}", params);
        if (collection.size() == 0)
            throw new FatalException("Collection with new object is empty. Error in permission service");

        if (!userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId())) {
            // Проверяем наличие прав на изменение и удаление
            DomainObjectPermission permissions = permissionService.getObjectPermission(domainObject.getId(), currentUserAccessor.getCurrentUserId());
            if (!permissions.getPermission().contains(DomainObjectPermission.Permission.Write))
                throw new FatalException("Not write permissions on new object. Error in permission service");
            if (!permissions.getPermission().contains(DomainObjectPermission.Permission.Delete))
                throw new FatalException("Not delete permissions on new object. Error in permission service");
        }

    }

}
