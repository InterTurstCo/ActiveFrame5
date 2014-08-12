package ru.intertrust.cm.test.extension;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint
public class AfterSaveTypeHierarchy implements BeforeSaveExtensionHandler {


    @Override
    public void onBeforeSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (domainObject.getTypeName().equals("employee") && domainObject.getString("name").startsWith("test_hierarchy") ){
            int saveCount = Integer.valueOf(domainObject.getString("certificate"));
            domainObject.setString("certificate", "" + (saveCount + 1));
        }
    }

}
