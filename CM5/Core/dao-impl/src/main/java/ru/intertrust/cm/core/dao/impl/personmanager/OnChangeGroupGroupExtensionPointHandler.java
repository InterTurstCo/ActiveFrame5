package ru.intertrust.cm.core.dao.impl.personmanager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "group_group")
public class OnChangeGroupGroupExtensionPointHandler implements AfterSaveExtensionHandler, AfterDeleteExtensionHandler {

    @Autowired
    private DomainObjectCacheService domainObjectCacheService; 

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        clearCollectionCache();
    }
    
    @Override
    public void onAfterDelete(DomainObject domainObject) {
        clearCollectionCache();
    }
    
    private void clearCollectionCache() {
        domainObjectCacheService.clearObjectCollectionByKey(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.CHILD_GROUPS.name());
        domainObjectCacheService.clearObjectCollectionByKey(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_CHILD_GROUPS.name());
        domainObjectCacheService.clearObjectCollectionByKey(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.ALL_PARENT_GROUPS.name());
        domainObjectCacheService.clearObjectCollectionByKey(DomainObjectCacheService.COLLECTION_CACHE_CATEGORY.GROUP_FOR_PERSON.name());
    }

}
