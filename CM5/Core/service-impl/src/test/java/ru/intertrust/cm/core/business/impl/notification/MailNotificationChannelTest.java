package ru.intertrust.cm.core.business.impl.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.ArgumentCaptor;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.model.NotificationException;

public class MailNotificationChannelTest {
    private MailNotificationChannel channel;
    private MailSenderWrapper wrapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws MessagingException {
        channel = new MailNotificationChannel() {
            @Override
            protected String getPersonLocale(Id personId) {
                return "RU";
            }

            @Override
            public Id findLocaleIdByName(String name) {
                return null;
            }
        };

        wrapper = mock(MailSenderWrapper.class);
        when(wrapper.getHost()).thenReturn("host");
        MimeMessage emptyMessage = new MimeMessage(mock(MimeMessage.class));
        when(wrapper.createMimeMessage()).thenReturn(emptyMessage);
        channel.mailSenderWrapper = wrapper;

        AccessControlService aclService = mock(AccessControlService.class);
        when(aclService.createSystemAccessToken(anyString())).thenReturn(mock(AccessToken.class));
        channel.accessControlService = aclService;

        DomainObject addressee = new GenericDomainObject();
        addressee.setString("email", "foo@bar");
        DomainObjectDao dao = mock(DomainObjectDao.class);
        when(dao.find(any(Id.class), any(AccessToken.class))).thenReturn(addressee);
        channel.domainObjectDao = dao;
    }

    @Test
    public void testHtmlWhenAvailable() throws MessagingException {
        NotificationTextFormer textFormer = mock(NotificationTextFormer.class);
        add(textFormer, "subject", "Test subject");
        add(textFormer, "body-html", "Body-HTML");
        channel.notificationTextFormer = textFormer;
        channel.send(null, "???", null, null, null);
        verify(wrapper).send(argThat(contentMatcher("Body-HTML", "text/html")));
    }

    @Test
    public void testPlainTextWhenNoHtml() {
        NotificationTextFormer textFormer = mock(NotificationTextFormer.class);
        add(textFormer, "subject", "Test subject");
        add(textFormer, "body", "Test message");
        channel.notificationTextFormer = textFormer;
        channel.send(null, "???", null, null, null);
        verify(wrapper).send(argThat(contentMatcher("Test message", "text/plain")));
    }

    @Test
    public void testAttachmentsInParent() {
        NotificationTextFormer textFormer = mock(NotificationTextFormer.class);
        add(textFormer, "subject", "Test subject");
        add(textFormer, "body", "Test message");
        // имя_фйла;идентификатор_объекта_с_вложением;имя_вложения
        add(textFormer, "attachment", "fileName;attachmentId;attachmentName");

        final IdService idService = mock(IdService.class);
        final DomainObjectMappingId id = new DomainObjectMappingId();
        when(idService.createId("attachmentId")).thenReturn(id);

        // По конфигурации, ДО имеет родителя, у которого есть поле с вложением
        DomainObject domainObject = new GenericDomainObject("child");
        when(channel.domainObjectDao.find(eq(id), any(AccessToken.class))).thenReturn(domainObject);

        final ConfigurationExplorer configurationExplorer = mock(ConfigurationExplorer.class);

        // Конфиг для нашего ДО
        DomainObjectTypeConfig childConfig = new DomainObjectTypeConfig();
        childConfig.setExtendsAttribute("parent");
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "child"))
                .thenReturn(childConfig);

        AttachmentTypesConfig attachmentTypesConfig = new AttachmentTypesConfig();
        AttachmentTypeConfig attachmentTypeConfig = new AttachmentTypeConfig();
        attachmentTypeConfig.setName("Parent's_Attachment");
        attachmentTypesConfig.getAttachmentTypeConfigs().add(attachmentTypeConfig);
        when(configurationExplorer.getAttachmentTypesConfigWithInherit(childConfig))
                .thenReturn(attachmentTypesConfig);

        DomainObjectTypeConfig parentConfig = new DomainObjectTypeConfig();
        parentConfig.setName("ParentName");
        parentConfig.setAttachmentTypesConfig(attachmentTypesConfig);
        when(configurationExplorer.getDomainObjectTypeConfig("parent"))
                .thenReturn(parentConfig);

        // В данном случае, моя цель дойти до этого вызова с корректными результатами, так что дальше смотреть не буду
        when(channel.domainObjectDao.findLinkedDomainObjects(eq(id), eq("Parent's_Attachment"), eq("ParentName"), any(AccessToken.class)))
                .thenReturn(Collections.emptyList());

        channel.configurationExplorer = configurationExplorer;
        channel.idService = idService;
        channel.notificationTextFormer = textFormer;
        channel.send(null, "???", null, null, null);

        ArgumentCaptor<String> argumentName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentParent = ArgumentCaptor.forClass(String.class);
        verify(channel.domainObjectDao).findLinkedDomainObjects(eq(id), argumentName.capture(), argumentParent.capture(), any(AccessToken.class));
        assertEquals("Parent's_Attachment", argumentName.getValue());
        assertEquals("ParentName", argumentParent.getValue());
    }

    @Test
    public void testAttachmentsInCurrent() {
        NotificationTextFormer textFormer = mock(NotificationTextFormer.class);
        add(textFormer, "subject", "Test subject");
        add(textFormer, "body", "Test message");
        // имя_фйла;идентификатор_объекта_с_вложением;имя_вложения
        add(textFormer, "attachment", "fileName;attachmentId;attachmentName");

        final IdService idService = mock(IdService.class);
        final DomainObjectMappingId id = new DomainObjectMappingId();
        when(idService.createId("attachmentId")).thenReturn(id);

        DomainObject domainObject = new GenericDomainObject("DO");
        when(channel.domainObjectDao.find(eq(id), any(AccessToken.class))).thenReturn(domainObject);

        final ConfigurationExplorer configurationExplorer = mock(ConfigurationExplorer.class);


        AttachmentTypesConfig attachmentTypesConfig = new AttachmentTypesConfig();
        AttachmentTypeConfig attachmentTypeConfig = new AttachmentTypeConfig();
        attachmentTypeConfig.setName("DO's_Attachment");
        attachmentTypesConfig.getAttachmentTypeConfigs().add(attachmentTypeConfig);


        // Конфиг для нашего ДО
        DomainObjectTypeConfig config = new DomainObjectTypeConfig();
        config.setName("DO config name");
        config.setAttachmentTypesConfig(attachmentTypesConfig);
        when(configurationExplorer.getConfig(DomainObjectTypeConfig.class, "DO"))
                .thenReturn(config);

        when(configurationExplorer.getAttachmentTypesConfigWithInherit(config))
                .thenReturn(attachmentTypesConfig);

        // В данном случае, моя цель дойти до этого вызова с корректными результатами, так что дальше смотреть не буду
        when(channel.domainObjectDao.findLinkedDomainObjects(eq(id), eq("DO's_Attachment"), eq("DO config name"), any(AccessToken.class)))
                .thenReturn(Collections.emptyList());

        channel.configurationExplorer = configurationExplorer;
        channel.idService = idService;
        channel.notificationTextFormer = textFormer;
        channel.send(null, "???", null, null, null);

        ArgumentCaptor<String> argumentName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentParent = ArgumentCaptor.forClass(String.class);
        verify(channel.domainObjectDao).findLinkedDomainObjects(eq(id), argumentName.capture(), argumentParent.capture(), any(AccessToken.class));
        assertEquals("DO's_Attachment", argumentName.getValue());
        assertEquals("DO config name", argumentParent.getValue());
    }

    @Test
    public void testExceptionWhenNoEither() {
        thrown.expect(NotificationException.class);
        NotificationTextFormer textFormer = mock(NotificationTextFormer.class);
        add(textFormer, "subject", "Test subject");
        channel.notificationTextFormer = textFormer;
        channel.send(null, "???", null, null, null);
    }

    private void add(NotificationTextFormer textFormer, String notificationPart, String message) {
        when(textFormer.format(anyString(), eq(notificationPart), any(Id.class), any(Id.class), anyString(), any(NotificationContext.class))).thenReturn(
                message);
        when(textFormer.contains(anyString(), eq(notificationPart), any(Id.class), anyString())).thenReturn(true);
    }

    private Matcher<MimeMessage> contentMatcher(final String content, final String contentType) {
        Matcher<MimeMessage> matcher = new BaseMatcher<MimeMessage>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof MimeMessage) {
                    MimeMessage message = (MimeMessage) item;
                    try {
                        return message.getContent().equals(content) &&
                                message.getDataHandler().getContentType().startsWith(contentType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(contentType + ": " + content);
            }
        };
        return matcher;
    }
}
