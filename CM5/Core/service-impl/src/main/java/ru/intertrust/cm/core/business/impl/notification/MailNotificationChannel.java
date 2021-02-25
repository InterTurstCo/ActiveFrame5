package ru.intertrust.cm.core.business.impl.notification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

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

    private static final String BODY_MAIL_PART = "body";

    private static final String BODY_HTML_MAIL_PART = "body-html";

    private static final String SUBJECT_MAIL_PART = "subject";

    private static final String ATTACHMENT_MAIL_PART = "attachment";

    private static final String EMAIL_FIELD = "email";

    /**
     * Зарезарвированное имя вложения, которое означает что имя надо
     * использовать такое же как и у вложения ДО
     */
    private static final String ATTACH_NAME = "ATTACH_NAME";

    private static final Logger logger = Logger.getLogger(MailNotificationChannel.class);

    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {
        MimeMessage message = null;
        String resipients = null;
        try {
            // В случае если в конфигурации не указан host то канал отключаем
            if (mailSenderWrapper.getHost() != null) {
                message = createMailMesssage(notificationType, senderId, addresseeId, context);
                // Почтовое сообщение могло быть не сформировано, например из за
                // отсутствия адресата
                if (message != null) {
                    //Адресаты для более понятной ошибки
                    Address[] addresses = message.getRecipients(MimeMessage.RecipientType.TO);
                    if (addresses != null && addresses.length > 0) {
                        resipients = addresses[0].toString();
                    }
                    mailSenderWrapper.send(message);
                    logger.debug("Notification sent by MailNotificationChannel notificationType=" + notificationType
                            + "; senderId="
                            + senderId + "; addresseeId=" + addresseeId + "; priority=" + priority + "; context=" + context);
                }
            }
        } catch (Exception ex) {
            //logger.error("Error send mail to " + resipients, ex);
            throw new MailNotificationException("Error send mail to " + resipients, ex);
        }
    }

    private MimeMessage createMailMesssage(String notificationType, Id senderId, Id addresseeId,
            NotificationContext context) throws MessagingException, UnsupportedEncodingException {
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(MAIL_NOTIFICATION_CHANNEL);
        InternetAddress sender = new InternetAddress();
        if (senderId != null && !mailSenderWrapper.isAlwaysUseDefaultSender()) {
            GenericDomainObject senderDO = (GenericDomainObject) domainObjectDao.find(senderId, systemAccessToken);
            sender.setAddress(senderDO.getString(EMAIL_FIELD));
            String personal = "";
            if (senderDO.getString("FirstName") != null && !senderDO.getString("FirstName").isEmpty()) {
                personal = senderDO.getString("FirstName");
            }

            if (senderDO.getString("LastName") != null && !senderDO.getString("LastName").isEmpty()) {
                if (!personal.isEmpty()) {
                    personal += " ";
                }
                personal += senderDO.getString("LastName");
            }

            sender.setPersonal(personal);
        }

        // Если у персоны небыл заполнен адрес или персона вообще не была
        // передана то устанавливаем значение по умолчанию
        if (sender.getAddress() == null) {
            sender.setAddress(mailSenderWrapper.getDefaultSender());
            if (sender.getPersonal() == null) {
                sender.setPersonal(mailSenderWrapper.getDefaultSenderName());
            }
        }

        mailSenderWrapper.getHost();
        GenericDomainObject addresseDO = (GenericDomainObject) domainObjectDao.find(addresseeId, systemAccessToken);
        String addresseMail = addresseDO.getString(EMAIL_FIELD);

        // Проверка на то что у адресата есть email. если нет то не формируем
        // сообщения
        if (addresseMail == null || addresseMail.isEmpty()) {
            logger.warn("Notification for addressee " + addresseeId + " not send. Person email field is empty.");
            return null;
        }

        Id locale = findLocaleIdByName(getPersonLocale(addresseeId));
        boolean html = notificationTextFormer.contains(notificationType, BODY_HTML_MAIL_PART, locale, MAIL_NOTIFICATION_CHANNEL);

        String subject = notificationTextFormer.format(notificationType, SUBJECT_MAIL_PART, addresseeId, locale,
                MAIL_NOTIFICATION_CHANNEL, context);
        String body = notificationTextFormer.format(notificationType, html ? BODY_HTML_MAIL_PART : BODY_MAIL_PART, addresseeId, locale,
                MAIL_NOTIFICATION_CHANNEL, context);

        MimeMessage mimeMessage = mailSenderWrapper.createMimeMessage();
        MimeMessageHelper message = null;

        // Проверяем наличие конфигурации вложения
        if (notificationTextFormer.contains(notificationType, ATTACHMENT_MAIL_PART, locale, MAIL_NOTIFICATION_CHANNEL)) {
            message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // Получаем вложение
            String attachment =
                    notificationTextFormer.format(notificationType, ATTACHMENT_MAIL_PART, addresseeId, locale,
                            MAIL_NOTIFICATION_CHANNEL, context);

            // В attachment должна находиться строка в формате
            // имя_фйла;идентификатор_объекта_с_вложением;имя_вложения
            String[] attachmentInfo = attachment.split(";");
            // Получаем объект к которому приаттачено вложение
            Id documentWithAttachmentId = idService.createId(attachmentInfo[1]);
            DomainObject documentWithAttachment = domainObjectDao.find(documentWithAttachmentId, systemAccessToken);
            DomainObjectTypeConfig documentWithAttachmentTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                    documentWithAttachment.getTypeName());

            // Получаем тип вложения к данному типу документа
            List<AttachmentTypeConfig> attachmentTypeConfigs =
                    configurationExplorer.getAttachmentTypesConfigWithInherit(documentWithAttachmentTypeConfig).getAttachmentTypeConfigs();
            for (AttachmentTypeConfig attachmentTypeConfig : attachmentTypeConfigs) {
                List<DomainObject> linkedAttachments = domainObjectDao.findLinkedDomainObjects(documentWithAttachmentId, attachmentTypeConfig.getName(),
                        documentWithAttachment.getTypeName(), systemAccessToken);
                for (DomainObject attachmentObject : linkedAttachments) {
                    if (attachmentObject.getString("name").matches(attachmentInfo[2])) {
                        ByteArrayResource streamSource = new ByteArrayResource(getAttachmentContent(attachmentObject));
                        String attachName = attachmentInfo[0];
                        if (attachmentInfo[0].equals(ATTACH_NAME)) {
                            attachName = attachmentObject.getString("name");
                        }
                        message.addAttachment(attachName, streamSource);
                    }
                }
            }
        } else {
            message = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        }

        message.setTo(addresseMail);
        message.setFrom(sender);
        message.setSubject(subject);
        message.setText(body, html);

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
            while ((read = contentStream.read(buffer)) > 0) {
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } catch (Exception ex) {
            throw new ReportServiceException("Error on get attachment body", ex);
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (inputStream != null) {
                    inputStream.close(true);
                }
            } catch (IOException ignoreEx) {
            }
        }
    }

    @Override
    public void send(String notificationType, String senderName, Id addresseeId, NotificationPriority priority, NotificationContext context) {
        send(notificationType, (Id) null, addresseeId, priority, context);
    }
}
