package ru.intertrust.cm.core.business.impl.notification;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Канал отправки уведомлений в папку "Входящие уведомления"
 * @author atsvetkov
 *
 */
@NotificationChannel(name = "InboxNotificationChannel",
        description = "Канал отправки уведомлений в папку \"Входящие уведомления\"")
public class InboxNotificationChannel extends NotificationChannelBase implements NotificationChannelHandle {
    private static final Logger logger = Logger.getLogger(InboxNotificationChannel.class);

    private static final String INBOX_NOTIFICATION_CHANNEL = "InboxNotificationChannel";

    private static final String BODY_INBOX_PART = "body";

    private static final String SUBJECT_INBOX_PART = "subject";

    private static final String LOCALE_EN = "EN";

    @Autowired
    protected CrudService crudService;
            
    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {
        Id locale = findLocaleIdByName(LOCALE_EN);
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(INBOX_NOTIFICATION_CHANNEL);

        String subject =
                notificationTextFormer.format(notificationType, SUBJECT_INBOX_PART, addresseeId, locale,
                        INBOX_NOTIFICATION_CHANNEL, context);
        String body =
                notificationTextFormer.format(notificationType, BODY_INBOX_PART, addresseeId, locale,
                        INBOX_NOTIFICATION_CHANNEL,
                        context);

        DomainObject notification = crudService.createDomainObject("notification");
        notification.setString("Subject", subject);
        notification.setString("Body", body);
        notification.setReference("From", senderId);
        notification.setReference("To", addresseeId);
        notification.setString("Priority", priority.toString());
        notification.setBoolean("New", true);

        notification = domainObjectDao.save(notification, systemAccessToken);

        logger.info("Notification sent by InboxNotificationChannel notificationType=" + notificationType
                + "; senderId="
                + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);

    }

}
