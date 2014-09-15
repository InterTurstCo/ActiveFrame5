package ru.intertrust.cm.core.business.impl.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
/**
 * 
 * @author atsvetkov
 *
 */
@ExtensionPoint
public class OnChangeNotificationSenderExtensionPoint extends NotificationSenderExtensionPointBase implements AfterSaveAfterCommitExtensionHandler {

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        sendNotifications(domainObject, changedFields);
    }

    @Override
    protected EventType getEventType() {
        return EventType.CHANGE;
    }

}
