package ru.intertrust.cm.core.business.api.notification;


import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.config.FindObjectSettings;

/**
 * Классы имплиментирующие NotificationContextObjectProducer могут на основание
 * существующих объектов в контексте создавать новые и возвращать их в методе getContextObject.
 */
public interface NotificationContextObjectProducer {
    
    Object getContextObject(NotificationContext context);
    
    void init(FindObjectSettings settings);
}