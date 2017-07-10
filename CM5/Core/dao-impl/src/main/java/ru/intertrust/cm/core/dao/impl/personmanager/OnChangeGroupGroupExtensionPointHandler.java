package ru.intertrust.cm.core.dao.impl.personmanager;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.extension.*;

import java.util.List;

@ExtensionPoint(filter = "group_group")
public class OnChangeGroupGroupExtensionPointHandler implements BeforeSaveExtensionHandler, AfterSaveExtensionHandler, BeforeDeleteExtensionHandler, AfterDeleteExtensionHandler {
    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    @Override
    public void onBeforeSave(DomainObject domainObject, List<FieldModification> changedFields) {
        clearCollectionCache();
    }

    @Override
    public void onBeforeDelete(DomainObject domainObject) {
        clearCollectionCache();
    }

    private void clearCollectionCache() {
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.CHILD_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_CHILD_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_PARENT_GROUPS.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP.name());
        domainObjectCacheService.clearCollection(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.PERSON_IN_GROUP_AND_SUBGROUP.name());
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (groupHierarchyChanged(changedFields)) {
            notifyGlobalCache();
        }
    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        notifyGlobalCache();
    }

    private void notifyGlobalCache() {
        globalCacheClient.notifyGroupHierarchyChanged();
    }

    private boolean groupHierarchyChanged(List<FieldModification> changedFields) {
        if (changedFields != null) {
            for (FieldModification fieldModification : changedFields) {
                final String name = fieldModification.getName();
                if (name.equalsIgnoreCase("parent_group_id") || name.equalsIgnoreCase("child_group_id")) {
                    return true;
                }
            }
        }
        return false;
    }
}
