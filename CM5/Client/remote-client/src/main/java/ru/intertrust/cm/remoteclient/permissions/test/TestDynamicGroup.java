package ru.intertrust.cm.remoteclient.permissions.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestDynamicGroup extends ClientBase {

    private CrudService.Remote crudService;
    private CollectionsService.Remote collectionService;
    private PersonManagementService.Remote prsonService;

    private Hashtable<String, String> roleMapping = new Hashtable<String, String>();
    private List<Id> createdObjects = new ArrayList<Id>();

    public static void main(String[] args) {
        try {
            TestDynamicGroup test = new TestDynamicGroup();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
            collectionService = (CollectionsService.Remote) getService("CollectionsServiceImpl", CollectionsService.Remote.class);
            prsonService = (PersonManagementService.Remote) getService("PersonManagementService", PersonManagementService.Remote.class);
            Long start = System.currentTimeMillis();

            testDynamicGroups();

            System.out.println("End ->"+ (System.currentTimeMillis() - start));
        } finally {
            writeLog();
        }
    }

    public void testDynamicGroups() throws Exception {
        InputStream fis = null;
        BufferedReader br = null;
        // Таблица с иденификаторами, для мепинга между объектами и их
        // идентификаторами
        Hashtable<String, Id> ids = new Hashtable<String, Id>();
        String[] owners = null;
        String[] roles = null;
        String[] entries = null;
        try {
            // Зачитываем конфигурацию
            String line;
            fis = new FileInputStream("test_dynamic_groups.csv");
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("windows-1251")));
            boolean start = false;
            while ((line = br.readLine()) != null) {
                // Делаем что то между START и END
                if (line.startsWith("START")) {
                    start = true;
                    continue;
                } else if (line.startsWith("STOP")) {
                    break;
                } else if (line.startsWith("OWNER")) {
                    owners = line.split(";");
                    continue;
                } else if (line.startsWith("ROLE")) {
                    roles = line.split(";");
                    continue;
                } else if (line.startsWith("ENTRIES")) {
                    entries = line.split(";");
                    continue;
                } else if (line.startsWith("MAPPING_GROUP")) {
                    initRoleMapping(line);
                    continue;
                }

                if (start) {
                    doOneLine(line, ids, owners, roles, entries);
                }
            }
            
            //Тест контекстной группы с фильтром (не для всех объектов типа создается дин группа)
            DomainObject doWithGroup = crudService.createDomainObject("test_type_33");
            doWithGroup.setString("name", "context_" + System.currentTimeMillis());
            doWithGroup.setReference("person", prsonService.getPersonId("admin"));
            doWithGroup = crudService.save(doWithGroup);
            
            DomainObject test33DynGroup = prsonService.findDynamicGroup("test_type_33_person_group", doWithGroup.getId());
            assertTrue("Dyn Group fith filter exists", test33DynGroup != null);
            
            List<DomainObject> personsInTest33DynGroup = prsonService.getPersonsInGroup(test33DynGroup.getId());
            assertTrue("Dyn Group fith filter members", personsInTest33DynGroup.size() == 1 && personsInTest33DynGroup.get(0).getString("login").equals("admin"));            
            
            DomainObject doWithoutGroup = crudService.createDomainObject("test_type_33");
            doWithoutGroup.setString("name", "not-context_" + System.currentTimeMillis());
            doWithoutGroup.setReference("person", prsonService.getPersonId("admin"));
            doWithoutGroup = crudService.save(doWithoutGroup);
            
            test33DynGroup = prsonService.findDynamicGroup("test_type_33_person_group", doWithoutGroup.getId());
            assertTrue("Dyn Group fith filter exists", test33DynGroup == null);
            
            log("Test dynamic group success");

        } finally {
            try {

                deleteBossInfo();
                deleteParentDepartmentInfo();
                deleteCreatedObjects();
                if (fis != null) {
                    fis.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // Игнорируем ошибку в finally чтобы не потерять ошибку в try
            }

        }

    }

    /**
     * Удаление всех раннее созданных объектов в обратном порядке
     */
    private void deleteCreatedObjects() {
        for (int i = createdObjects.size() -1 ; i > -1; i--) {
            try{
                log("delete >> " + crudService.find(createdObjects.get(i)).toString());
                crudService.delete(createdObjects.get(i));
            }catch (Exception e){
                throw e;
            }
        }
    }

    private void initRoleMapping(String line) {
        String[] roleMappingStrArray = line.split(";");
        for (int i = 3; i < roleMappingStrArray.length; i++) {
            roleMapping.put(roleMappingStrArray[i], roleMappingStrArray[i + 1]);
            i++;
        }
    }

    private void deleteBossInfo() {
        Hashtable<String, Object> attributes = new Hashtable<String, Object>();
        attributes.put("Boss", new NullId());
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery("select t.id from Department t");
        for (IdentifiableObject row : collection) {
            if (createdObjects.contains(row.getId())){
                setDomainObjectAttribute(row.getId(), attributes);
            }
        }
        collection = collectionService.findCollectionByQuery("select t.id from Organization t");
        for (IdentifiableObject row : collection) {
            if (createdObjects.contains(row.getId())){
                setDomainObjectAttribute(row.getId(), attributes);
            }
        }
    }

    private void deleteParentDepartmentInfo() {
        Hashtable<String, Object> attributes = new Hashtable<String, Object>();
        attributes.put("ParentDepartment", new NullId());
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery("select t.id from Department t");
        for (IdentifiableObject row : collection) {
            setDomainObjectAttribute(row.getId(), attributes);
        }
    }

    /*
    private void deleteTestUsers() {
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery("select id, login from person");
        for (IdentifiableObject row : collection) {
            if (row.getString("Login").startsWith("test_")) {
                crudService.delete(row.getId());
            }
        }
    }
    */

    private void doOneLine(String line, Hashtable<String, Id> ids, String[] owners, String[] roles, String[] entries) throws Exception {
        String etalonResult = null;
        try {
            
            long start = System.currentTimeMillis();
            
            String[] arguments = line.split(";");
            String commandString = arguments[0];
            etalonResult = arguments[2];
            String command = null;
            int firstArg = -1;
            String firstArgType = null;
            int secondArg = -1;
            String secondArgType = null;
            int therdArg = -1;
            String therdArgType = null;

            Pattern pattern = Pattern.compile("^([CSD])([ZBODEP])(\\d)(?:([ODEP])(\\d))?(?:([ODEP])(\\d))?$");
            Matcher matcher = pattern.matcher(commandString);

            if (matcher.matches()) {
                command = matcher.group(1);
                firstArgType = matcher.group(2);
                firstArg = Integer.parseInt(matcher.group(3));
                if (matcher.group(4) != null) {
                    secondArgType = matcher.group(4);
                    secondArg = Integer.parseInt(matcher.group(5));
                }
                if (matcher.group(6) != null) {
                    therdArgType = matcher.group(6);
                    therdArg = Integer.parseInt(matcher.group(7));
                }
            }

            Hashtable<String, Object> attributes = new Hashtable<String, Object>();
            if (command.equals("C")) {
                Id id = null;
                if (firstArgType.equals("O")) {
                    // Создание организации
                    attributes.put("Name", "Organization" + firstArg);
                    id = createDomainObject("Organization", attributes);
                } else if (firstArgType.equals("D")) {
                    // Создание подразделения
                    attributes.put("Name", "Department" + firstArg);
                    attributes.put("Organization", ids.get("O" + secondArg));
                    if (therdArg > -1) {
                        attributes.put("ParentDepartment", ids.get("D" + therdArg));
                    }
                    id = createDomainObject("Department", attributes);
                } else if (firstArgType.equals("E")) {
                    // Создание сотрудника
                    attributes.put("Name", "Employee" + firstArg);
                    attributes.put("Login", "Employee" + System.currentTimeMillis());
                    attributes.put("Department", ids.get("D" + secondArg));
                    attributes.put("Position", "Бухгалтер");
                    attributes.put("Phone", "+7" + System.nanoTime());
                    
                    id = createDomainObject("Employee", attributes);
                } else if (firstArgType.equals("Z")) {
                    // Создание делегирования
                    attributes.put("person", ids.get("E" + therdArg));
                    attributes.put("delegate", ids.get("E" + secondArg));
                    id = createDomainObject("Delegation", attributes);
                }
                ids.put(firstArgType + firstArg, id);
            } else if (command.equals("S")) {
                if (firstArgType.equals("B")) {
                    if (secondArgType.equals("D")) {
                        // Установка руководителя для подразделения
                        attributes.put("Boss", ids.get("E" + firstArg));
                        setDomainObjectAttribute(ids.get("D" + secondArg), attributes);
                    } else if (secondArgType.equals("O")) {
                        // Установка руководителя для организации
                        attributes.put("Boss", ids.get("E" + firstArg));
                        setDomainObjectAttribute(ids.get("O" + secondArg), attributes);
                    }
                } else if (firstArgType.equals("D")) {
                    if (secondArgType.equals("O")) {
                        // Установка организации у подразделения
                        attributes.put("Organization", ids.get("O" + secondArg));
                        setDomainObjectAttribute(ids.get("D" + firstArg), attributes);
                    }
                    if (secondArgType.equals("D")) {
                        // Установка вышестоящего подразделения у подразделения
                        attributes.put("ParentDepartment", ids.get("D" + secondArg));
                        setDomainObjectAttribute(ids.get("D" + firstArg), attributes);
                    }
                } else if (firstArgType.equals("E")) {
                    if (secondArgType.equals("D")) {
                        // Изменение подразделения у сотрудника
                        attributes.put("Department", ids.get("D" + secondArg));
                        setDomainObjectAttribute(ids.get("E" + firstArg), attributes);
                    }
                }
            } else if (command.equals("D")) {
                if (firstArgType.equals("D")) {
                    // Удаление подразделения
                    crudService.delete(ids.get("D" + firstArg));
                    createdObjects.remove(ids.get("D" + firstArg));
                }
            }

            validateOneLine(commandString, arguments, ids, owners, roles, entries);
            log("Validate line complete [" + line + "] at " + (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            if (etalonResult != null && etalonResult.length() > 0) {
                int result = Integer.parseInt(etalonResult);
                if (result == 1) {
                    throw new Exception(ex);
                } else {
                    log("Validate line complete [" + line + "]. Error \"" + ex.getMessage() + "\" correct throws");
                }
            }
        }
    }

    private void setDomainObjectAttribute(Id id, Hashtable<String, Object> attributes) {
        DomainObject domainObject = crudService.find(id);
        for (Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getValue() instanceof String) {
                domainObject.setString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Id) {
                domainObject.setReference(entry.getKey(), (Id) entry.getValue());
            } else if (entry.getValue() instanceof NullId) {
                domainObject.setReference(entry.getKey(), (Id) null);
            }
        }
        crudService.save(domainObject);
    }

    private Id createDomainObject(String type, Hashtable<String, Object> attributes) {
        DomainObject domainOject = crudService.createDomainObject(type);
        for (Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getValue() instanceof String) {
                domainOject.setString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Id) {
                domainOject.setReference(entry.getKey(), (Id) entry.getValue());
            }
        }
        domainOject = crudService.save(domainOject);
        createdObjects.add(domainOject.getId());
        return domainOject.getId();
    }

    private void validateOneLine(String command, String[] arguments, Hashtable<String, Id> ids, String[] owners, String[] roles, String[] entries)
            throws Exception {
        String owner = null;
        String role = null;
        String entry = null;
        for (int i = 3; i < entries.length; i++) {
            String etalonUsers = "";
            String etalonRoles = "";
            if (arguments.length > i) {
                etalonUsers = arguments[i];
            }

            if (arguments.length > i + 1) {
                etalonRoles = arguments[i + 1];
            }
            if (owners.length > i - 1 && owners[i].length() > 0) {
                owner = owners[i];
            }
            if (roles.length > i - 1 && roles[i].length() > 0) {
                role = roles[i];
            }

            // Получение id роли из базы
            DomainObject baseRole = null;
            if (ids.get(owner) != null) {
                baseRole = prsonService.findDynamicGroup(roleMapping.get(role), ids.get(owner));
            }

            List<DomainObject> baseRoleUsers = new ArrayList<DomainObject>();
            List<DomainObject> baseRoleRoles = new ArrayList<DomainObject>();

            // В случае если роль существует то получаем ее состав
            if (baseRole != null) {
                // Получение из базы состав пользователей в роли
                baseRoleUsers = prsonService.getPersonsInGroup(baseRole.getId());
                // Получение из базы состав ролей в роле
                baseRoleRoles = prsonService.getChildGroups(baseRole.getId());
            }

            // Сравнение результатов
            compareRoles("[" + command + "] compare roles for " + owner + " role " + role + ". ", etalonRoles, baseRoleRoles, ids);
            compareUsers("[" + command + "] compare users for " + owner + " role " + role + ". ", etalonUsers, baseRoleUsers, ids);

            i++;
        }
    }

    private void compareUsers(String command, String etalonUsers, List<DomainObject> baseRoleUsers, Hashtable<String, Id> ids) throws Exception {
        List<Id> userIds = getIdList(baseRoleUsers);
        compareList(command, "E", userIds, etalonUsers, ids);
    }

    private void compareRoles(String command, String etalonRoles, List<DomainObject> baseRoleRoles, Hashtable<String, Id> ids) throws Exception {
        List<Id> roleIds = getIdList(baseRoleRoles);
        List<Id> testIds = new ArrayList<Id>();
        if (etalonRoles != null && etalonRoles.length() > 0) {
            String[] roleKeysArray = etalonRoles.split(",");
            for (String element : roleKeysArray) {
                testIds.add(getRoleId(element, ids));
            }
        }
        compareList(command, roleIds, testIds, ids);
    }

    private Id getRoleId(String roleKey, Hashtable<String, Id> ids) {
        String prefix = null;
        if (roleKey.startsWith("0") || roleKey.startsWith("1") || roleKey.startsWith("2")) {
            prefix = "O";
        } else if (roleKey.startsWith("3") || roleKey.startsWith("4") || roleKey.startsWith("5")) {
            prefix = "D";
        } else if (roleKey.startsWith("6")) {
            prefix = "E";
        }

        Id id = ids.get(prefix + roleKey.charAt(1));

        DomainObject role = prsonService.findDynamicGroup(
                roleMapping.get("C" + roleKey.charAt(0)), id);
        return role.getId();
    }

    private List<Id> getIdList(List<DomainObject> listDomainObjects) {
        List<Id> result = new ArrayList<Id>();
        for (DomainObject domainObject : listDomainObjects) {
            result.add(domainObject.getId());
        }
        return result;
    }

    /**
     * Сравнение списка идентификаторов
     * 
     * @param roles
     * @param string
     * @throws Exception
     */
    private void compareList(String description, String keyPrefix, List<Id> compareIds, String roleKeysAsString, Hashtable<String, Id> ids) throws Exception {
        List<Id> testIds = new ArrayList<Id>();
        if (roleKeysAsString != null && roleKeysAsString.length() > 0) {
            String[] roleKeysArray = roleKeysAsString.split(",");
            for (String element : roleKeysArray) {
                testIds.add(ids.get(keyPrefix + element));
            }
        }

        compareList(description, compareIds, testIds, ids);
    }

    private void compareList(String description, List<Id> compareIds, List<Id> testIds, Hashtable<String, Id> ids) throws Exception {
        // Проверка идентичности количества элементов
        assertTrue(description + " Collection count not equal {" + compareIds.toString() + "}{" + testIds.toString() + "}", compareIds.size() == testIds.size());

        // Проверка наличия всех элементов тестового списка в проверяемом
        for (Id id : testIds) {
            assertTrue(description + " Collection count not equal {" + compareIds.toString() + "}{" + testIds.toString() + "}", compareIds.contains(id));
        }

        // Проверка отсутствия лишних элементов в проверяемом списке
        for (Id id : compareIds) {
            assertTrue(description + " Collection count not equal {" + compareIds.toString() + "}{" + testIds.toString() + "}", testIds.contains(id));
        }
    }

    protected void deleteTestDomainObject(String type) {
        IdentifiableObjectCollection dataSet = collectionService.findCollectionByQuery("select id from " + type, 0, 0);
        for (IdentifiableObject row : dataSet) {
            crudService.delete(row.getId());
        }
    }

    protected void deleteTestDomainObject(String type, String condition) {
        IdentifiableObjectCollection dataSet = collectionService.findCollectionByQuery("select id from " + type + " where " + condition, 0, 0);
        for (IdentifiableObject row : dataSet) {
            crudService.delete(row.getId());
        }
    }

    private class NullId {
    }

}
