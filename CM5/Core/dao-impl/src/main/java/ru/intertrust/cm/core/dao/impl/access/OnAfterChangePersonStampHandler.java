package ru.intertrust.cm.core.dao.impl.access;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "person_stamp")
public class OnAfterChangePersonStampHandler implements AfterSaveExtensionHandler, AfterDeleteExtensionHandler {

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        clearCache(deletedDomainObject.getReference("person"));
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        clearCache(domainObject.getReference("person"));
    }

    private void clearCache(Id personId){
        globalCacheClient.notifyPersonGroupChanged(personId);
    }
}
