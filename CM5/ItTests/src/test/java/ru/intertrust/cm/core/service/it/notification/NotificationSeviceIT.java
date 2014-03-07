package ru.intertrust.cm.core.service.it.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.service.it.IntegrationTestBase;

/**
 * Интеграционный тест сервиса отправки сообщений
 * @author larin
 * 
 */
@RunWith(Arquillian.class)
public class NotificationSeviceIT extends IntegrationTestBase {

    @EJB
    private NotificationService.Remote notificationService;

    @EJB
    private CrudService.Remote crudService;

    @EJB
    private PersonService.Remote personService;

    @EJB
    private PersonManagementService.Remote personManagementService;

    @EJB
    private CollectionsService.Remote collectionService;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            initBase();
        } finally {
            loginContext.logout();
        }
    }

    @After
    public void dispose() throws LoginException {

    }

    @Test
    public void sendToPerson() throws InterruptedException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            NotificationTestChannel testChannel = new NotificationTestChannel();
            NotificationAddresseePerson addressee =
                    new NotificationAddresseePerson(personService.findPersonByLogin("person1").getId());
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_PERSON";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(1000);

            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person1").getId(),
                    priority, context));
        } finally {
            loginContext.logout();
        }

    }

    @Test
    public void sendToGroup() throws InterruptedException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            NotificationTestChannel testChannel = new NotificationTestChannel();
            NotificationAddresseeGroup addressee =
                    new NotificationAddresseeGroup(personManagementService.getGroupId("Administrators"));
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_GROUP";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(1000);

            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("admin").getId(),
                    priority, context));
        } finally {
            loginContext.logout();
        }

    }

    @Test
    public void sendToDynamicGroup() throws InterruptedException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            NotificationTestChannel testChannel = new NotificationTestChannel();
            NotificationAddresseeDynamicGroup addressee =
                    new NotificationAddresseeDynamicGroup("DepartmentEmployees", getDepartment("Подразделение 1").getId());
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_DYN_GROUP";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(1000);

            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person1").getId(),
                    priority, context));
            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person3").getId(),
                    priority, context));
            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person4").getId(),
                    priority, context));
            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person5").getId(),
                    priority, context));

        } finally {
            loginContext.logout();
        }
    }

    @Test
    public void sendToContextRole() throws InterruptedException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            
            //Создаем внутренний документ
            DomainObject intDoc = crudService.createDomainObject("Internal_Document");
            intDoc.setString("Name", "Name-" + System.currentTimeMillis());
            intDoc.setReference("Registrant",  personService.findPersonByLogin("person1"));
            intDoc = crudService.save(intDoc);
            
            NotificationTestChannel testChannel = new NotificationTestChannel();
            NotificationAddresseeContextRole addressee =
                    new NotificationAddresseeContextRole("Registrator", intDoc.getId());
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_CONTEXT_ROLE";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(1000);

            Assert.assertTrue(testChannel.contains(notificationType, senderId, personService.findPersonByLogin("person1").getId(),
                    priority, context));

        } finally {
            loginContext.logout();
        }
    }
    
    
    private DomainObject getOrganization(String name) {
        return findDomainObject("Organization", "Name", name);
    }

    private DomainObject getDepartment(String name) {
        return findDomainObject("Department", "Name", name);
    }
    
    private DomainObject findDomainObject(String type, String field, String fieldValue) {
        String query = "select t.id from " + type + " t where t." + field + "='" + fieldValue + "'";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        DomainObject result = null;
        if (collection.size() > 0) {
            result = crudService.find(collection.get(0).getId());
        }
        return result;
    }
}
