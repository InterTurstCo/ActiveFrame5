package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.notification.NotificationContextObjectProducer;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;

public class NotificationContextObjectProducerImpl implements NotificationContextObjectProducer {

    @Override
    public Object getContextObject(NotificationContext context) {
        Dto document = context.getContextObject("document");
        if (document instanceof DomainObjectAccessor) {
            return ((DomainObjectAccessor) document).getId();
        }
        return null;
    }
}
