package ru.intertrust.cm.core.business.impl.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventTriggerImpl.EventType;


public interface NotificationSenderAsync {
    void sendNotifications(DomainObject domainObject, EventType eventType, List<FieldModification> changedFields);
}
