package ru.intertrust.cm.core.business.impl.notification;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
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
