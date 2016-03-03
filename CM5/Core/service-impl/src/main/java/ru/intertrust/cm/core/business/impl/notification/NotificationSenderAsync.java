package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventType;

import java.util.List;


public interface NotificationSenderAsync {
    void sendNotifications(List<SendNotificationInfo> sendNotificationInfos);
    void sendNotifications(DomainObject domainObject, EventType eventType, List<FieldModification> changedFields);
}
