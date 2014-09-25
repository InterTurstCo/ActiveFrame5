package ru.intertrust.cm.test.notification;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.notification.NotificationContextObjectProducer;
import ru.intertrust.cm.core.config.FindObjectSettings;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;

public class TestNotificationContextObjectProducerImpl implements NotificationContextObjectProducer {
    private TestNotificationContextObjectProducerSettings settings;
    @Override
    public Object getContextObject(NotificationContext context) {
        assert(settings != null);
        Dto document = context.getContextObject("document");
        if (document instanceof DomainObjectAccessor) {
            return ((DomainObjectAccessor) document).getId();
        }
        return null;
    }

    @Override
    public void init(FindObjectSettings settings) {
        this.settings = (TestNotificationContextObjectProducerSettings)settings;
    }
}
