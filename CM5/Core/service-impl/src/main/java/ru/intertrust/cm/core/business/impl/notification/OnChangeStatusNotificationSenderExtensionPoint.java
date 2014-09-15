package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusAfterCommitExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterChangeStatusExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * 
 * @author atsvetkov
 *
 */
@ExtensionPoint
public class OnChangeStatusNotificationSenderExtensionPoint extends NotificationSenderExtensionPointBase implements AfterChangeStatusAfterCommitExtentionHandler {

    @Override
    public void onAfterChangeStatus(DomainObject domainObject) {
        sendNotifications(domainObject, null);
    }

    protected EventType getEventType() {
        return EventType.CHANGE_STATUS;
    }

}
