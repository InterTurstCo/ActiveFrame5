package ru.intertrust.cm.core.business.impl.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
/**
 * 
 * @author atsvetkov
 *
 */
public class OnChangeNotificationSenderExtensionPoint implements AfterSaveExtensionHandler {

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {       
        
    }

}
