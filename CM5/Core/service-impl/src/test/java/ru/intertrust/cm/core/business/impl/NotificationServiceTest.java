package ru.intertrust.cm.core.business.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.impl.NotificationServiceTest.NotificationServiceTestLoader;
import ru.intertrust.cm.core.tools.NotificationAddresseeConverter;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NotificationServiceTestLoader.class)
public class NotificationServiceTest {
    @Autowired
    private ApplicationContext context;

    @Before
    public void init() {
        SpringApplicationContext appContext = new SpringApplicationContext();
        appContext.setApplicationContext(context);
    }

    @Test
    public void testNotificationAddresseeConverter() throws Exception {
        NotificationAddresseeConverter converter = new NotificationAddresseeConverter();
        Id id1 = new RdbmsId(1, 1);
        Id id2 = new RdbmsId(2, 1);
        Id id3 = new RdbmsId(3, 1);
        Id id4 = new RdbmsId(4, 1);

        //Заполняем и сериализуем
        converter.addPerson(id1)
                .addGroup(id2)
                .addDynamicGroup("dyn-group-1", id3)
                .addContextRole("context-role-1", id4);
        String addresseeAsString = converter.toString();
        
        //Десериализуем и проверяем
        NotificationAddresseeConverter converter2 = NotificationAddresseeConverter.load(addresseeAsString);
        Assert.assertTrue(converter2.getAddresseeList().size() == 4);

        int containsAddressee = 0;
        
        for (NotificationAddressee addressee : converter2.getAddresseeList()) {
            if (addressee instanceof NotificationAddresseePerson) {
                NotificationAddresseePerson notificationAddressee = (NotificationAddresseePerson) addressee;
                Assert.assertTrue(notificationAddressee.getPersonId().equals(id1));
                containsAddressee |= 1;
            } else if (addressee instanceof NotificationAddresseeGroup) {
                NotificationAddresseeGroup notificationAddressee = (NotificationAddresseeGroup) addressee;
                Assert.assertTrue(notificationAddressee.getGroupId().equals(id2));
                containsAddressee |= 2;
            } else if (addressee instanceof NotificationAddresseeDynamicGroup) {
                NotificationAddresseeDynamicGroup notificationAddressee = (NotificationAddresseeDynamicGroup) addressee;
                Assert.assertTrue(notificationAddressee.getContextId().equals(id3));
                Assert.assertTrue(notificationAddressee.getGroupName().equals("dyn-group-1"));
                containsAddressee |= 4;
            } else if (addressee instanceof NotificationAddresseeContextRole) {
                NotificationAddresseeContextRole notificationAddressee = (NotificationAddresseeContextRole) addressee;
                Assert.assertTrue(notificationAddressee.getContextId().equals(id4));
                Assert.assertTrue(notificationAddressee.getRoleName().equals("context-role-1"));
                containsAddressee |= 8;
            }
        }
        
        Assert.assertTrue(containsAddressee == 15);
    }

    @Configuration
    public static class NotificationServiceTestLoader {
        @Bean
        public static IdService idService() {
            IdService idService = mock(IdService.class);
            when(idService.createId(Mockito.anyString())).then(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return new RdbmsId((String) invocation.getArguments()[0]);
                }
            });
            return idService;
        }
    }
}
