package ru.intertrust.cm.core.service.it.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

import ru.intertrust.cm.core.business.api.*;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.notification.*;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.core.config.NotificationAddresseConfig;
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

    @EJB
    private ScheduleService.Remote schedulerService;


    /**
     * Предотвращает загрузку данных для каждого теста. Данные загружаются один раз для всех тестов в данном классе.
     */
    private boolean isDataLoaded = false;


    @Before
    public void init() throws IOException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            if (!isDataLoaded) {
                initBase();
                importTestData("test-data/import-system-profile.csv");
                importTestData("test-data/import-person-profile.csv");
                importTestData("test-data/import-string-value.csv");
                importTestData("test-data/import-employee-prof.csv");

                setPersonProfile("person1", "002");
                setPersonProfile("person3", "003");
                setPersonProfile("person4", "004");
                setPersonProfile("person5", "005");
                setPersonProfile("admin", "006");

                isDataLoaded = true;
            }
        } finally {
            loginContext.logout();
        }
    }

    private void setPersonProfile(String person, String ppName) {
        Id personId = personService.findPersonByLogin(person).getId();
        DomainObject personProfileDo = findDomainObject("profile", "name", ppName);
        DomainObject person1do = crudService.find(personId);
        person1do.setReference("profile", personProfileDo.getId());
        crudService.save(person1do);
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
            Id person1id = personService.findPersonByLogin("person1").getId();
            NotificationAddresseePerson addressee =
                    new NotificationAddresseePerson(person1id);
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_PERSON";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(500);

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId, person1id, priority, context));
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

            //Делаем несколько попыток, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(500);
            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId, 
                    personService.findPersonByLogin("admin").getId(), priority, context));
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
            Thread.currentThread().sleep(500);

            //Делаем несколько попыток, так как отправка происходит в асинхронном режиме
            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId,
                    personService.findPersonByLogin("person1").getId(), priority, context));

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId,
                    personService.findPersonByLogin("person3").getId(), priority, context));

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId,
                    personService.findPersonByLogin("person4").getId(), priority, context));

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId,
                    personService.findPersonByLogin("person5").getId(), priority, context));
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
            Thread.currentThread().sleep(500);

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId, 
                    personService.findPersonByLogin("person1").getId(), priority, context));

        } finally {
            loginContext.logout();
        }
    }


    @Test
    public void sendToPersonProf() throws InterruptedException, LoginException {
        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        try {
            NotificationTestChannel testChannel = new NotificationTestChannel();
            Id person1Id = personService.findPersonByLogin("person001").getId();
            NotificationAddresseePerson addressee =
                    new NotificationAddresseePerson(person1Id);
            List<NotificationAddressee> addresseeList = new ArrayList<NotificationAddressee>();
            addresseeList.add(addressee);
            NotificationContext context = new NotificationContext();
            context.addContextObject("contextObject", getOrganization("Организация 1").getId());

            String notificationType = "TEST_NOTIFICATION_TO_PERSON";
            Id senderId = personService.findPersonByLogin("admin").getId();
            NotificationPriority priority = NotificationPriority.HIGH;

            notificationService.sendOnTransactionSuccess(notificationType, senderId, addresseeList, priority, context);

            //Спим секунду, так как отправка происходит в асинхронном режиме
            Thread.currentThread().sleep(500);

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, senderId, person1Id, priority, context));

        } finally {
            loginContext.logout();
        }

    }

    @Test
    public void testNotificationSenderEvaluate() throws InterruptedException, LoginException {
        LoginContext loginContext = login("person4", "admin");
        loginContext.login();
        try {        
            DomainObject organization = createOrganizationDomainObject();
            DomainObject savedOrganization = crudService.save(organization);
            DomainObject department = createDepartmentDomainObject(savedOrganization);
            DomainObject savedDepartment = crudService.save(department);
            
        } finally {
            loginContext.logout();
        }
    }

    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("organization_test");
        organizationDomainObject.setString("Name", "Organization" + System.currentTimeMillis());
        return organizationDomainObject;
    }

    private DomainObject createDepartmentDomainObject(DomainObject savedOrganizationObject) {
        DomainObject departmentDomainObject = crudService.createDomainObject("department_test");
        departmentDomainObject.setString("Name", "department" + System.currentTimeMillis());
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
    }

    @Test
    public void sendOnSchedule(String[] args) throws Exception {

        LoginContext loginContext = login("admin", "admin");
        loginContext.login();
        DomainObject task = null;
        try {

            NotificationTestChannel testChannel = new NotificationTestChannel();

            task = getTaskByName("NotificationScheduleTaskTest");
            if (task == null) {
                task =
                        schedulerService.createScheduleTask(
                                "ru.intertrust.cm.core.business.impl.notification.NotificationScheduleTask",
                                "NotificationScheduleTaskTest");
            }

            String notificationType = "TEST_NOTIFICATION_SCHEDULE";
            NotificationPriority priority = NotificationPriority.HIGH;

            NotificationTaskConfig testparam = new NotificationTaskConfig();
            testparam.setName(notificationType);
            testparam.setPriority(priority);
            testparam.setTaskMode(NotificationTaskMode.BY_DOMAIN_OBJECT);

            //По всем организациям
            FindObjectsConfig findDomainObject = new FindObjectsConfig();
            findDomainObject.setFindObjectType(new FindObjectsQueryConfig("select id from organization"));
            testparam.setFindDomainObjects(findDomainObject);

            FindObjectsConfig findPersonObject = new FindObjectsConfig();
            findPersonObject.setFindObjectType(new FindObjectsQueryConfig("select id from person where login='person001'"));
            
            NotificationAddresseConfig notificationAddresseConfig = new NotificationAddresseConfig();
            notificationAddresseConfig.setFindPerson(findPersonObject);
            
            testparam.setNotificationAddresseConfig(notificationAddresseConfig);

            schedulerService.setTaskParams(task.getId(), testparam);

            //Ждем чтобы было без 5 секунд до начала запуска задач по расписанию, для синхронизации теста и заданий
            while (Calendar.getInstance().get(Calendar.SECOND) != 55) {
                Thread.currentThread().sleep(500);
            }

            schedulerService.enableTask(task.getId());

            Thread.currentThread().sleep(5000);

            Assert.assertTrue(tryToGetNotification(testChannel, notificationType, 
                    null, personService.findPersonByLogin("person001").getId(), priority, null));

        } finally {

            //Отключение задач
            if (task != null)
                schedulerService.disableTask(task.getId());
            loginContext.logout();
        }
    }

    private DomainObject getTaskByName(String name) {
        List<DomainObject> taskList = schedulerService.getTaskList();

        DomainObject result = null;
        for (DomainObject task : taskList) {
            if (task.getString("name").equals(name)) {
                result = task;
                break;
            }
        }
        return result;
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

    /**
     * Получение сообщения
     * @param testChannel
     *            канал
     * @param notificationType
     *            тип
     * @param senderId
     *            отправитель
     * @param addresseeId
     *            получатель
     * @param priority
     *            приоритет
     * @param context
     *            контекст
     * @return результат: true, если получить удалось, false - иначе
     */
    private boolean tryToGetNotification(NotificationTestChannel testChannel, String notificationType,
            Id senderId, Id addresseeId, NotificationPriority priority, NotificationContext context) {
        boolean bGet = false;
        int cnt = 200;
        while (cnt-- > 0 && !(bGet = testChannel.contains(notificationType, senderId,
                addresseeId, priority, context))) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                //do nothing
            }
        }
        return bGet;
    }
}
