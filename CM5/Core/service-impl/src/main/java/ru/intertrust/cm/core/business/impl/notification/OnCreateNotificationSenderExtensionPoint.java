package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * 
 * @author atsvetkov
 *
 */
@ExtensionPoint
public class OnCreateNotificationSenderExtensionPoint extends NotificationSenderExtensionPointBase implements AfterCreateAfterCommitExtentionHandler{

    @Override
    public void onAfterCreate(DomainObject domainObject) {
        sendNotifications(domainObject, null);
    }

    @Override
    protected EventType getEventType() {
        return EventType.CREATE;
    }

}
