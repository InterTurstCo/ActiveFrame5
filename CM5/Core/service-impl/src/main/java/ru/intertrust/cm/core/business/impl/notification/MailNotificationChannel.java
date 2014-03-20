package ru.intertrust.cm.core.business.impl.notification;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.MailNotificationException;

/**
 * 
 * @author atsvetkov
 *
 */
@NotificationChannel(name = "MailNotificationChannel", description = "Канал отправки по электронной почте")
public class MailNotificationChannel implements NotificationChannelHandle {

    private static final String MAIL_NOTIFICATION_CHANNEL = "MailNotificationChannel";

    private static final String BODY_MAIL_PART = "body";

    private static final String SUBJECT_MAIL_PART = "subject";

    private static final String EMAIL_FIELD = "email";

    private static final String LOCALE_EN = "EN";

    private static final Logger logger = Logger.getLogger(MailNotificationChannel.class);

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private NotificationTextFormer notificationTextFormer;

    @Autowired
    private CollectionsService collectionService;

    private SimpleMailMessage mailTemplate;

    public void setMailTemplate(SimpleMailMessage mailTemplate) {
        this.mailTemplate = mailTemplate;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setNotificationTextFormer(NotificationTextFormer notificationTextFormer) {
        this.notificationTextFormer = notificationTextFormer;
    }

    public void setCollectionService(CollectionsService.Remote collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {

        try {
            SimpleMailMessage message = createMailMesssage(notificationType, senderId, addresseeId, context);
            this.mailSender.send(message);
        } catch (Exception ex) {
            throw new MailNotificationException(ex.getMessage());
        }
        logger.info("Send notification by MailNotificationChannel notificationType=" + notificationType + "; senderId="
                + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);
    }

    private SimpleMailMessage createMailMesssage(String notificationType, Id senderId, Id addresseeId,
            NotificationContext context) {
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(MAIL_NOTIFICATION_CHANNEL);
        String senderMail = null;
        if (senderId != null) {
            GenericDomainObject senderDO = (GenericDomainObject) domainObjectDao.find(senderId, systemAccessToken);
            senderMail = senderDO.getString(EMAIL_FIELD);
        } else {
            senderMail = mailTemplate.getFrom();
        }
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(addresseMail);
        message.setFrom(senderMail);
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private Id findLocaleIdByName(String localeName) {
        String query = "select t.id from locale t where t.name='" + localeName + "'";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        Id locale = null;
        if (collection.size() > 0) {
            locale = collection.get(0).getId();
        }
        return locale;
    }

}
