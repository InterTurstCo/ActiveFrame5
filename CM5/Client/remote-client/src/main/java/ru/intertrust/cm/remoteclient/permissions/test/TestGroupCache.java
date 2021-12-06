package ru.intertrust.cm.remoteclient.permissions.test;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.remoteclient.ClientBase;

import javax.naming.NamingException;

public class TestGroupCache extends ClientBase {
    private static final String TEST_PASSWORD = "111111";

    public static void main(String[] args) {
        try {
            TestGroupCache test = new TestGroupCache();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            // Создание пользователя
            DomainObject testPerson = createTestPerson();

            // выполнение модификации персоны новым пользователем
            assertFalse("Edit by new person", canEdit(testPerson.getId(), testPerson.getString("login")));

            // Проверка кэширования в группе "Administrators"
            checkGroupCache("Administrators", testPerson);

            // Проверка кэширования в группе "Superusers"
            checkGroupCache("Superusers", testPerson);

            if (hasError) {
                System.out.println("Test failed");
            } else {
                System.out.println("Test success");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkGroupCache(String groupName, DomainObject testPerson) throws Exception {
        // Добавление пользователя в группу administrators
        Id adminGroup = getPersonManagementService().getGroupId(groupName);
        getPersonManagementService().addPersonToGroup(adminGroup, testPerson.getId());

        // выполнение модификации персоны новым пользователем
        assertTrue("Edit by new person in " + groupName + " group",
                canEdit(testPerson.getId(), testPerson.getString("login")));

        // удаление пользователя из группы
        getPersonManagementService().remotePersonFromGroup(adminGroup, testPerson.getId());

        // выполнение модификации персоны новым пользователем после удаления из группы, должна быть ошибка
        assertFalse("Edit by new person after delete from " + groupName + " group",
                canEdit(testPerson.getId(), testPerson.getString("login")));
    }

    private DomainObject createTestPerson() throws NamingException {
        DomainObject testPerson = getCrudService().createDomainObject("person");
        testPerson.setString("login", "person_" + System.currentTimeMillis());
        testPerson.setString("first_name", testPerson.getString("login"));
        testPerson = getCrudService().save(testPerson);

        DomainObject authInfo = getCrudService().createDomainObject("authentication_info");
        authInfo.setString("user_uid", testPerson.getString("login"));
        authInfo.setString("password", TEST_PASSWORD);
        getCrudService().save(authInfo);

        return testPerson;
    }

    private CrudService getCrudService() throws NamingException {
        return (CrudService) getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private CrudService getCrudService(String login, String password) throws NamingException {
        return (CrudService) getService("CrudServiceImpl", CrudService.Remote.class, login, password);
    }

    private PersonManagementService getPersonManagementService() throws NamingException {
        return (PersonManagementService) getService(
                "PersonManagementService", PersonManagementService.Remote.class);
    }

    private boolean canEdit(Id testPersonId, String login){
        try {
            DomainObject testPerson = getCrudService(login, TEST_PASSWORD).find(testPersonId);
            testPerson.setString("email", "" + System.currentTimeMillis() + "@inntust.ru");
            getCrudService(login, TEST_PASSWORD).save(testPerson);
            return true;
        }catch(Exception ex){
            return false;
        }
    }
}
