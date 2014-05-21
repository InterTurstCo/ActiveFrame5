package ru.intertrust.cm.core.business.impl.notification;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.MailNotificationException;

/**
 * 
 * @author atsvetkov
 * 
 */
@NotificationChannel(name = "MailNotificationChannel", description = "Канал отправки по электронной почте")
public class MailNotificationChannel extends NotificationChannelBase implements NotificationChannelHandle {

    private static final String MAIL_NOTIFICATION_CHANNEL = "MailNotificationChannel";

    private static final String BODY_MAIL_PART = "body";

    private static final String SUBJECT_MAIL_PART = "subject";

    private static final String EMAIL_FIELD = "email";

    private static final String LOCALE_EN = "EN";

    private static final Logger logger = Logger.getLogger(MailNotificationChannel.class);

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {

        try {
            //В случае если в конфигурации не указан host то канал отключаем
            if (mailSenderWrapper.getHost() != null) {
                MimeMessage message = createMailMesssage(notificationType, senderId, addresseeId, context);
                mailSenderWrapper.send(message);
                logger.info("Notification sent by MailNotificationChannel notificationType=" + notificationType
                        + "; senderId="
                        + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);
            }
        } catch (Exception ex) {
            throw new MailNotificationException("Error send mail", ex);
        }
    }

    private MimeMessage createMailMesssage(String notificationType, Id senderId, Id addresseeId,
            NotificationContext context) throws MessagingException {
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(MAIL_NOTIFICATION_CHANNEL);
        String senderMail = null;
        if (senderId != null) {
            GenericDomainObject senderDO = (GenericDomainObject) domainObjectDao.find(senderId, systemAccessToken);
            senderMail = senderDO.getString(EMAIL_FIELD);
        } else {
            senderMail = mailSenderWrapper.getDefaultSender();
        }

        mailSenderWrapper.getHost();
        GenericDomainObject addresseDO = (GenericDomainObject) domainObjectDao.find(addresseeId, systemAccessToken);
        String addresseMail = addresseDO.getString(EMAIL_FIELD);

        Id locale = findLocaleIdByName(LOCALE_EN);
        String subject =
                notificationTextFormer.format(notificationType, SUBJECT_MAIL_PART, addresseeId, locale,
                        MAIL_NOTIFICATION_CHANNEL, context);
        String body =
                notificationTextFormer.format(notificationType, BODY_MAIL_PART, addresseeId, locale,
                        MAIL_NOTIFICATION_CHANNEL,
                        context);

        MimeMessage mimeMessage = mailSenderWrapper.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setTo(addresseMail);
        message.setFrom(senderMail);
        message.setSubject(subject);
        message.setText(body);
        return mimeMessage;
    }

}
