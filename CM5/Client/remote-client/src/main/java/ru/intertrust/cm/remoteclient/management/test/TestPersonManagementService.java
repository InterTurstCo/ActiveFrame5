package ru.intertrust.cm.remoteclient.management.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.remoteclient.ClientBase;

import static org.junit.Assert.assertEquals;
/**
 * Класс для тестирования сервиса управления пользователями и группами
 * @author larin
 * 
 */
public class TestPersonManagementService extends ClientBase {
    private PersonManagementService.Remote personService;
    private CrudService.Remote crudService;
    private Hashtable<String, Id> groupIds = new Hashtable<String, Id>();
    private Hashtable<String, Id> personIds = new Hashtable<String, Id>();
    private String suffix = String.valueOf(System.currentTimeMillis());

    public static void main(String[] args) {
        try {
            TestPersonManagementService test = new TestPersonManagementService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getPersonLogin(int num) {
        return "per_" + suffix + "_" + num;
    }

    private String getGroupName(int num) {
        return "grp_" + suffix + "_" + num;
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            personService =
                    (PersonManagementService.Remote) getService("PersonManagementService",
                            PersonManagementService.Remote.class);
            crudService =
                    (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);

            
            // Создание массива пользователей и групп
            for (int i = 1; i < 15; i++) {
                // Поиск пользователя
                Id personId = null;
                try{
                    personId = personService.getPersonId(getPersonLogin(i));
                }catch(Exception ex){
                    //Пользователь не найден
                }
                if (personId == null) {
                    // Создание пользователя
                    DomainObject person = crudService.createDomainObject("Person");
                    person.setString("Login", getPersonLogin(i));
                    person.setString("FirstName", getPersonLogin(i));
                    person.setString("LastName", getPersonLogin(i));
                    person.setString("EMail", getPersonLogin(i) + "@intertrast.ru");
                    person = crudService.save(person);
                    personId = person.getId();
                }
                personIds.put("person" + i, personId);

                // Поиск группы
                Id groupId = personService.getGroupId(getGroupName(i));
                if (groupId == null) {
                    // Создание группы
                    DomainObject group = crudService.createDomainObject("User_Group");
                    group.setString("group_name", getGroupName(i));
                    group = crudService.save(group);
                    groupId = group.getId();
                }
                groupIds.put("group" + i, groupId);

                // Добавление персоны в группу
                if (!personService.isPersonInGroup(groupId, personId)) {
                    personService.addPersonToGroup(groupId, personId);
                }

                // Проверка вхождения пользователя в группу
                assertTrue("Person in group", personService.isPersonInGroup(groupId, personId));
            }

            // Строим иерархию
            addGroupToGroup("group1", "group3");
            assertTrue("Group in group",
                    personService.isGroupInGroup(groupIds.get("group1"), groupIds.get("group3"), false));
            
            addGroupToGroup("group1", "group4");
            addGroupToGroup("group2", "group5");
            addGroupToGroup("group2", "group6");
            addGroupToGroup("group3", "group7");
            addGroupToGroup("group3", "group8");
            addGroupToGroup("group4", "group9");
            addGroupToGroup("group4", "group10");
            addGroupToGroup("group5", "group11");
            addGroupToGroup("group5", "group12");
            addGroupToGroup("group6", "group13");
            addGroupToGroup("group6", "group14");

            // Проверка вхождения с учетом иерархии
            assertTrue("Group in group",
                    personService.isGroupInGroup(groupIds.get("group1"), groupIds.get("group7"), true));
            assertTrue("Group in group",
                    personService.isGroupInGroup(groupIds.get("group2"), groupIds.get("group14"), true));
            assertFalse("Group in group",
                    personService.isGroupInGroup(groupIds.get("group2"), groupIds.get("group9"), true));

            // Проверка вхождения пользователя в группы с учетом иерархии
            assertTrue("Person in group",
                    personService.isPersonInGroup(groupIds.get("group1"), personIds.get("person10")));
            assertFalse("Person in group",
                    personService.isPersonInGroup(groupIds.get("group2"), personIds.get("person10")));

            // Проверка списков
            assertTrue("Person list", personService.getPersonsInGroup(groupIds.get("group1")).size() == 1);
            assertTrue("Person list", personService.getAllPersonsInGroup(groupIds.get("group1")).size() == 7);
            //Групп больше так как прибавляется группа AllPersons и контекстная группа для пользователя Person
            assertTrue("Group list", personService.getPersonGroups(personIds.get("person9")).size() == 7);
            assertTrue("Group list", personService.getAllParentGroup(groupIds.get("group12")).size() == 2);
            assertTrue("Group list", personService.getChildGroups(groupIds.get("group2")).size() == 2);
            assertTrue("Group list", personService.getAllChildGroups(groupIds.get("group2")).size() == 6);

            // Рушим иерархию
            personService.remoteGroupFromGroup(groupIds.get("group1"), groupIds.get("group3"));
            personService.remoteGroupFromGroup(groupIds.get("group1"), groupIds.get("group4"));
            personService.remoteGroupFromGroup(groupIds.get("group2"), groupIds.get("group5"));
            personService.remoteGroupFromGroup(groupIds.get("group2"), groupIds.get("group6"));
            personService.remoteGroupFromGroup(groupIds.get("group3"), groupIds.get("group7"));
            personService.remoteGroupFromGroup(groupIds.get("group3"), groupIds.get("group8"));
            personService.remoteGroupFromGroup(groupIds.get("group4"), groupIds.get("group9"));
            personService.remoteGroupFromGroup(groupIds.get("group4"), groupIds.get("group10"));
            personService.remoteGroupFromGroup(groupIds.get("group5"), groupIds.get("group11"));
            personService.remoteGroupFromGroup(groupIds.get("group5"), groupIds.get("group12"));
            personService.remoteGroupFromGroup(groupIds.get("group6"), groupIds.get("group13"));
            personService.remoteGroupFromGroup(groupIds.get("group6"), groupIds.get("group14"));

            // Проверка вхождения с учетом иерархии
            assertFalse("Group in group",
                    personService.isGroupInGroup(groupIds.get("group1"), groupIds.get("group7"), true));
            assertFalse("Group in group",
                    personService.isGroupInGroup(groupIds.get("group2"), groupIds.get("group14"), true));

            // Проверка вхождения пользователя в группы с учетом иерархии
            assertFalse("Person in group",
                    personService.isPersonInGroup(groupIds.get("group1"), personIds.get("person10")));

            // Добавление альтернативного имени
            DomainObject person = crudService.find(personIds.get("person" + 1));
            createAltUid(person.getId(), "U=" + person.getString("login"), "ldap", 0);
            createAltUid(person.getId(), person.getString("login"), "basic", 1);
            // Получение альтернативного имени
            List<String> altUnid = personService.getPersonAltUids(person.getString("login"), "basic", "ldap");
            assertTrue("Alt uids",altUnid != null && altUnid.size() == 1 && altUnid.get(0).equals("U=" + person.getString("login")));

            // Удаление пользователей и групп
            for (int i = 1; i < 15; i++) {
                // Удаляем пользователя из группы
                personService.remotePersonFromGroup(groupIds.get("group" + i), personIds.get("person" + i));

                // Проверка вхождения пользователя в группу
                assertFalse("Person in group",
                        personService.isPersonInGroup(groupIds.get("group" + i), personIds.get("person" + i)));

                crudService.delete(groupIds.get("group" + i));
//                crudService.delete(personIds.get("person" + i));
            }

            if (hasError){
                log("Test FAILURE");
            }else{
                log("Test OK");
            }
        } finally {
            writeLog();
        }
    }

    private DomainObject createAltUid(Id personId, String altLogin, String altLoginType, long idx){
        DomainObject altUid = crudService.createDomainObject("person_alt_uids");
        altUid.setReference("person", personId);
        altUid.setString("alter_uid", altLogin);
        altUid.setString("alter_uid_type", altLoginType);
        altUid.setLong("idx", idx);
        altUid = crudService.save(altUid);
        return altUid;
    }

    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("Organization");
        organizationDomainObject.setString("Name", "Organization" + new Date());
        return organizationDomainObject;
    }

    private DomainObject createDepartmentDomainObject(DomainObject savedOrganizationObject) {
        DomainObject departmentDomainObject = crudService.createDomainObject("Department");
        departmentDomainObject.setString("Name", "department1");
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
    }
    
    private List<Id> getIdList(List<DomainObject> domainObjects) {
        List<Id> foundIds = new ArrayList<Id>();
        for (DomainObject domainObject : domainObjects) {
            foundIds.add(domainObject.getId());
        }
        return foundIds;
    }
    
    public void testFindAndDeleteList() throws Exception {

        DomainObject organization1 = createOrganizationDomainObject();
        
        DomainObject savedOrganization1 = crudService.save(organization1);

        DomainObject department = createDepartmentDomainObject(savedOrganization1);
        DomainObject savedDepartment = crudService.save(department);
        
        Id[] objects = new Id[] {savedDepartment.getId(), savedOrganization1.getId()};
        List<Id> objectIds = Arrays.asList(objects);

        List<DomainObject> foundObjects = crudService.find(objectIds);

        List<Id> foundIds = getIdList(foundObjects);
        assertNotNull(foundObjects);
        assertTrue("Find and Delete", foundObjects.size() == 2);

        assertTrue("Find and Delete", foundIds.contains(savedDepartment.getId()));
        assertTrue("Find and Delete", foundIds.contains(savedOrganization1.getId()));

        crudService.delete(objectIds);
        foundObjects = crudService.find(objectIds);
        assertTrue("Find and Delete", foundObjects.size() == 0);

    }
    
    private int getStaticGroupCount(List<DomainObject> groups) {
        int result = 0;
        for (DomainObject group : groups) {
            if (group.getReference("object_id") == null) {
                result++;
            }
        }
        return result;
    }

    private void addGroupToGroup(String parent, String child) {
        if (!personService.isGroupInGroup(groupIds.get(parent), groupIds.get(child), false)) {
            personService.addGroupToGroup(groupIds.get(parent), groupIds.get(child));
        }
    }

}
