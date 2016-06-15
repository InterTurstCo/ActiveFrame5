package ru.intertrust.cm.core.business.impl.notification;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
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
    private static final String NOTIFICATION_DO = "notification";

    private static final Logger logger = Logger.getLogger(InboxNotificationChannel.class);

    private static final String BODY_INBOX_PART = "body";

    private static final String SUBJECT_INBOX_PART = "subject";

    @Autowired
    protected CrudService crudService;
            
    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {
        Id locale = findLocaleIdByName(getPersonLocale(addresseeId));
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(INBOX_NOTIFICATION_CHANNEL);

        String subject =
                notificationTextFormer.format(notificationType, SUBJECT_INBOX_PART, addresseeId, locale,
                        INBOX_NOTIFICATION_CHANNEL, context);
        String body =
                notificationTextFormer.format(notificationType, BODY_INBOX_PART, addresseeId, locale,
                        INBOX_NOTIFICATION_CHANNEL,
                        context);
        DomainObject notification = createNotification(senderId, addresseeId, subject, body, priority);
        
        //Смотрим есть ли контекст с именем документ
        Dto document = context.getContextObject("document");
        if (document != null && document instanceof Id){
            notification.setReference("context_object", (Id)document);
        }

        notification = domainObjectDao.save(notification, systemAccessToken);

        logger.debug("Notification sent by InboxNotificationChannel notificationType=" + notificationType
                + "; senderId="
                + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);

    }

    private DomainObject createNotification(Id senderId, Id addresseeId, String subject, String body,
            NotificationPriority priority) {
        DomainObject notification = crudService.createDomainObject(NOTIFICATION_DO);
        notification.setString("Subject", subject);
        notification.setString("Body", body);
        notification.setReference("From", senderId);
        notification.setReference("To", addresseeId);
        notification.setString("Priority", priority.toString());
        notification.setBoolean("New", true);
        return notification;
    }

    @Override
    public void send(String notificationType, String senderName, Id addresseeId, NotificationPriority priority, NotificationContext context) {
        //Игнорируем строкового отправителя, подставляем null, тоесть от имени система
        send(notificationType, (Id)null, addresseeId, priority, context);
    }

}
