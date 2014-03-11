package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusExtentionHandler;

/**
 * 
 * @author atsvetkov
 *
 */
public class OnChangeStatusNotificationSenderExtensionPoint implements AfterChangeStatusExtentionHandler {

    @Override
    public void onAfterChangeStatus(DomainObject domainObject) {
        
    }

}
