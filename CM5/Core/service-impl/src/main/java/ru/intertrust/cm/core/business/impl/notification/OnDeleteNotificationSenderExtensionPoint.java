package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * 
 * @author atsvetkov
 *
 */
@ExtensionPoint
public class OnDeleteNotificationSenderExtensionPoint extends NotificationSenderExtensionPointBase implements AfterDeleteAfterCommitExtensionHandler {

    @Override
    public void onAfterDelete(DomainObject domainObject) {
        sendNotifications(domainObject, null);
        
    }

    @Override
    protected EventType getEventType() {
        return EventType.DELETE;
    }
}
