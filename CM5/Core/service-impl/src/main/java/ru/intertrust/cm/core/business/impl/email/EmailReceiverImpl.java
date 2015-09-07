package ru.intertrust.cm.core.business.impl.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.SearchTerm;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.integration.mail.SearchTermStrategy;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.EmailReceiver;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.email.EmailReceiverConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * Имплементация сервиса подключения к сервисам pop3 и imap и получение
 * сообщений, с последующим сохранением этих сообщений в базе в типе
 * email_message
 * @author larin
 * 
 */
public class EmailReceiverImpl implements EmailReceiver {
    private static final Logger logger = LoggerFactory.getLogger(EmailReceiverImpl.class);
    public static final int DEFAULT_MAX_MESSAGES_COUNT = 100;

    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private AttachmentService attachmentService;

    /**
     * Метод просматривает почтовый ящик и сохраняет новые письма в базе, а
     * письма ранее сохраненные удаляет с сервера
     */
    @Override
    public List<Id> receive(EmailReceiverConfig config) throws FatalException {
        try {
            List<Id> result = new ArrayList<Id>();
            AbstractMailReceiver receiver = null;
            Integer port = config.getPort();
            EmailReceiverConfig.EmailReceiverProtocol protocol = config.getProtocol();

            //Определяем протокол и порт, по умолчанию протокол pop3 порт 110
            if (protocol == null) {
                protocol = EmailReceiverConfig.EmailReceiverProtocol.pop3;
            }

            if (protocol.equals(EmailReceiverConfig.EmailReceiverProtocol.pop3)) {
                if (port == null) {
                    if (config.getEncryptionType() != null) {
                        port = 995;
                    } else {
                        port = 110;
                    }
                }
                receiver = new Pop3MailReceiver(config.getHost(), port, config.getLogin(), config.getPassword());
            } else if (protocol.equals(EmailReceiverConfig.EmailReceiverProtocol.imap)) {
                if (port == null) {
                    if (config.getEncryptionType() != null) {
                        port = 993;
                    } else {
                        port = 143;
                    }
                }
                receiver = new ImapMailReceiver(
                        new URLName("imap", config.getHost(), port, "INBOX", config.getLogin(), config.getPassword()).toString());
                ((ImapMailReceiver) receiver).setSearchTermStrategy(new NotDeletedSearchTermStrategy());
            } else {
                throw new FatalException("Mail receiver not support protocol " + config.getProtocol());
            }

            //Определяем способ шифрования
            if (config.getEncryptionType() != null) {
                Properties javaMailProperties = null;
                javaMailProperties = new Properties();
                if (config.getEncryptionType().equals(EmailReceiverConfig.EmailReceiverEncryptionType.ssl)) {
                    javaMailProperties.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                } else if (config.getEncryptionType().equals(EmailReceiverConfig.EmailReceiverEncryptionType.tls)) {
                    javaMailProperties.setProperty("mail." + protocol + ".starttls.enable", "true");
                }
                javaMailProperties.put("mail." + protocol + ".socketFactory.fallback", "false");
                javaMailProperties.put("mail." + protocol + ".ssl.enable", "true");
                receiver.setJavaMailProperties(javaMailProperties);
            }

            receiver.setBeanFactory(context);
            receiver.afterPropertiesSet();

            //Устанавливаем количество одновременно обрабатываемых сообщений, по умолчаниюю DEFAULT_MAX_MESSAGES_COUNT
            int maxMessages = DEFAULT_MAX_MESSAGES_COUNT;
            if (config.getMaxMessages() != null) {
                maxMessages = config.getMaxMessages();
            }

            //В этой сесии c почтовым сервером ничего не удаляем на сервере
            receiver.setShouldDeleteMessages(false);
            receiver.setMaxFetchSize(maxMessages);
            Message[] messages = receiver.receive();
            logger.debug("Receive " + messages.length + " messages");

            List<String> messagesForDelete = new ArrayList<String>();
            for (Message message : messages) {
                String messageId = message.getHeader("Message-ID")[0];
                logger.debug("Process message " + messageId);

                //Проверка на то что письмо получено ранее
                if (messageReveived(messageId)) {
                    //Письмо уже было обработано в предыдущем запуске задания, в этом запуске мы удаляем его с почтового сервера
                    //Сохраняем письмо в списке для удаления
                    logger.debug("Message " + messageId + " finded in base, mark it to delete");
                    messagesForDelete.add(messageId);
                } else {
                    //Сохранение письма
                    DomainObject messageObject = new GenericDomainObject("email_message");
                    messageObject.setString("message_id", messageId);
                    messageObject.setString("from", getAddressesAsString(message.getFrom()));
                    messageObject.setString("to", getAddressesAsString(message.getAllRecipients()));
                    messageObject.setString("subject", message.getSubject());

                    MailContent mailContent = new MailContent();
                    processContent(mailContent, message);
                    messageObject.setString("body", mailContent.body);

                    messageObject = domainObjectDao.save(messageObject, accessControlService.createSystemAccessToken(EmailReceiverImpl.class.getName()));
                    result.add(messageObject.getId());
                    logger.debug("Create message object " + messageId);

                    //Сохраняфем вложения
                    for (MailAttachment mailAttachment : mailContent.attachments) {
                        setAttachment(messageObject.getId(), mailAttachment);
                    }
                }
            }

            //Открываем новую сесию с почтовым сервером и удаляем сообщения помеченные для удаления
            if (messagesForDelete.size() > 0) {
                receiver.setShouldDeleteMessages(true);
                //Ищем те сообщения messageId которых нашли в базе ранее полученных сообщений
                ExpressionParser parser = new SpelExpressionParser();
                Expression expression = parser.parseExpression(getDeletedMessageListExpression(messagesForDelete) + ".contains(getHeader('Message-ID')[0])");
                receiver.setSelectorExpression(expression);
                //При получение сообщения удаляются на сервере
                messages = receiver.receive();
                logger.debug("Delete " + messages.length + " messages from server");
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error reseive message", ex);
        }
    }

    /**
     * Сохранение вложения
     * @param messageId
     * @param mailAttachment
     * @return
     * @throws IOException
     */
    private DomainObject setAttachment(Id messageId, MailAttachment mailAttachment) throws IOException {
        DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(messageId, "email_message_attachment");
        attachment.setString("Name", mailAttachment.fileName);
        attachment.setString("mimetype", mailAttachment.contentType);
        DirectRemoteInputStream directRemoteInputStream = new DirectRemoteInputStream(mailAttachment.inputStream, false);

        RemoteInputStream remoteInputStream;
        DomainObject result = attachmentService.saveAttachment(directRemoteInputStream, attachment);
        return result;
    }

    /**
     * Анализ почтового сообщения. Выденение в нем текста сообщения, вложений,
     * встроенных изображений или других объектов, сообщений в альтернативном
     * формате
     * @param mailContent
     * @param part
     * @throws MessagingException
     * @throws IOException
     */
    private void processContent(MailContent mailContent, Part part) throws MessagingException, IOException {
        if (part.getContent() instanceof MimeMultipart) {
            MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();

            //Цикл по частям сообщения
            for (int x = 0; x < mimeMultipart.getCount(); x++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(x);

                String disposition = bodyPart.getDisposition();

                if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {

                    DataHandler handler = bodyPart.getDataHandler();

                    MailAttachment attachment = new MailAttachment();
                    attachment.contentType = handler.getContentType();
                    attachment.fileName = handler.getName();
                    attachment.inputStream = handler.getInputStream();
                    mailContent.attachments.add(attachment);

                } else {
                    processContent(mailContent, bodyPart);
                }
            }

        } else {
            if (part.isMimeType("text/plain")) {
                mailContent.body = part.getContent().toString();
            } else {
                //Фрагмент письма любого другово формата кроме text/plain сохраняем дополнительно как вложение
                MailAttachment attachment = new MailAttachment();
                attachment.contentType = part.getContentType();
                attachment.fileName = part.getFileName();
                attachment.inputStream = part.getInputStream();
                mailContent.attachments.add(attachment);

                //Дополнительные обработки для известных mimetype
                if (part.isMimeType("text/html") && mailContent.body.isEmpty()) {
                    mailContent.body = part.getContent().toString();
                } else if (part.isMimeType("text/rtf") && mailContent.body.isEmpty()) {
                    mailContent.body = part.getContent().toString();
                }

                //Формирование имени для фрагментов без имени
                if (attachment.fileName == null) {
                    if (part.isMimeType("text/html")) {
                        attachment.fileName = "html" + mailContent.attachments.size() + ".html";
                    } else if (part.isMimeType("text/rtf")) {
                        attachment.fileName = "rtf" + mailContent.attachments.size() + ".rtf";
                    }
                }
            }
        }
    }

    /**
     * Формирования списка из message-id для SPEL выражения
     * @param messaheForDelete
     * @return
     */
    private String getDeletedMessageListExpression(List<String> messaheForDelete) {
        String result = "{";
        for (String messageId : messaheForDelete) {
            if (result.length() > 1) {
                result += ",";
            }
            result += "'" + messageId + "'";
        }
        result += "}";
        return result;
    }

    /**
     * Проверка наличия данного сообщения в базе
     * @return
     */
    private boolean messageReveived(String messageId) {
        String query = "select id from email_message where message_id = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(messageId));
        IdentifiableObjectCollection collection =
                collectionsDao.findCollectionByQuery(query, params, 0, 0, accessControlService.createSystemAccessToken(EmailReceiverImpl.class.getName()));
        return collection.size() > 0;
    }

    /**
     * Формирование строки интернет адреса для сохранения в базе
     * @param addresses
     * @return
     */
    private String getAddressesAsString(Address[] addresses) {
        String result = "";
        if (addresses == null) {
            result = "unknown";
        } else {
            for (Address address : addresses) {
                if (!result.isEmpty()) {
                    result += "; ";
                }
                if (address instanceof InternetAddress) {
                    InternetAddress internetAddress = (InternetAddress) address;
                    if (internetAddress.getPersonal() != null) {
                        result += internetAddress.getPersonal() + " ";
                    }
                    result += "[" + internetAddress.getAddress() + "]";
                } else {
                    result += address.toString();
                }
            }
        }
        return result;
    }

    /**
     * Класс представление вложения в письмо
     * @author larin
     *
     */
    private class MailAttachment {
        String contentType;
        String fileName;
        InputStream inputStream;
    }

    /**
     * Класс контейнер электронного письма
     * @author larin
     *
     */
    private class MailContent {
        String body = "";
        List<MailAttachment> attachments = new ArrayList<MailAttachment>();
    }

    /**
     * Вспомогательный класс для формирования стратегии работы с imap ящиком для поиска сообщений. Игнорируются только удаленные сообщения
     * @author larin
     *
     */
    private class NotDeletedSearchTermStrategy implements SearchTermStrategy {

        public SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
            SearchTerm searchTerm = null;
            boolean recentFlagSupported = false;
            if (supportedFlags != null) {
                recentFlagSupported = supportedFlags.contains(Flags.Flag.RECENT);
                if (recentFlagSupported) {
                    searchTerm = new FlagTerm(new Flags(Flags.Flag.RECENT), true);
                }
                if (supportedFlags.contains(Flags.Flag.DELETED)) {
                    NotTerm notDeleted = new NotTerm(new FlagTerm(new Flags(Flags.Flag.DELETED), true));
                    if (searchTerm == null) {
                        searchTerm = notDeleted;
                    }
                    else {
                        searchTerm = new AndTerm(searchTerm, notDeleted);
                    }
                }
            }
            return searchTerm;
        }

    }
}
