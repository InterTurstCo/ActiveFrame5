package ru.intertrust.cm.core.dao.impl.personmanager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "group_group")
public class OnChangeGroupGroupExtensionPointHandler implements BeforeSaveExtensionHandler, BeforeDeleteExtensionHandler {

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

}
