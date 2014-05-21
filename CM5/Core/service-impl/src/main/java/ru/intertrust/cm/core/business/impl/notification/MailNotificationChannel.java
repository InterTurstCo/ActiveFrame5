package ru.intertrust.cm.core.business.impl.notification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.MailNotificationException;
import ru.intertrust.cm.core.model.ReportServiceException;

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
    
    private static final String ATTACHMENT_MAIL_PART = "attachment";

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
                        MAIL_NOTIFICATION_CHANNEL, context);

        MimeMessage mimeMessage = mailSenderWrapper.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        message.setTo(addresseMail);
        message.setFrom(senderMail);
        message.setSubject(subject);
        message.setText(body);

        //Проверяем наличие конфигурации вложения
        if (notificationTextFormer.contains(notificationType, ATTACHMENT_MAIL_PART, locale, MAIL_NOTIFICATION_CHANNEL)){
            //Получаем вложение
            String attachment =
                notificationTextFormer.format(notificationType, ATTACHMENT_MAIL_PART, addresseeId, locale,
                        MAIL_NOTIFICATION_CHANNEL, context);
        
            //В attachment должна находиться строка в формате имя_фйла,идентификатор_объекта_с_вложением,имя_вложения      
            String[] attachmentInfo = attachment.split(";"); 
            //Получаем объект к которому приаттачено вложение
            Id documentWithAttachmentId = idService.createId(attachmentInfo[1]);
            DomainObject documentWithAttachment = domainObjectDao.find(documentWithAttachmentId, systemAccessToken);
            DomainObjectTypeConfig documentWithAttachmentTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, documentWithAttachment.getTypeName());
            
            //Получаем тип вложения к данному типу документа
            for (AttachmentTypeConfig attachmentTypeConfig : documentWithAttachmentTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                List<DomainObject> linkedAttachments = domainObjectDao.findLinkedDomainObjects(documentWithAttachmentId, attachmentTypeConfig.getName(), documentWithAttachment.getTypeName(), systemAccessToken);
                for (DomainObject attachmentObject : linkedAttachments) {
                    if (attachmentObject.getString("name").equalsIgnoreCase(attachmentInfo[2])){
                        ByteArrayResource streamSource = new ByteArrayResource(getAttachmentContent(attachmentObject)); 
                        message.addAttachment(attachmentInfo[0],  streamSource);
                    }
                }
            }
        }
        return mimeMessage;
    }
    
    private byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();
            
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0){
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } catch (Exception ex) {
            throw new ReportServiceException("Error on get attachment body", ex);
        } finally {
            try {
                contentStream.close();
                inputStream.close(true);
            } catch (IOException ignoreEx) {
            }
        }
    }
}
