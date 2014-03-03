package ru.intertrust.cm.core.business.impl.notification;

import org.apache.log4j.Logger;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;

@NotificationChannel(name="MailNotificationChannel", description="Канал отправки по электронной почте")
public class MailNotificationChannel implements NotificationChannelHandle {
    private static final Logger logger = Logger.getLogger(MailNotificationChannel.class);

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {
        logger.info("Send notification by MailNotificationChannel notificationType=" + notificationType + "; senderId="
                + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);
    }

}
