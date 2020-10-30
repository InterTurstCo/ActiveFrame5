package ru.intertrust.cm.remoteclient.permissions.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.NamingException;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestStamp extends ClientBase {

    private static final String TEST_PASSWORD = "111111";
    private static final String STAMP_1_NAME = "stamp1";
    private static final String STAMP_2_NAME = "stamp2";

    private DomainObject stamp1;
    private DomainObject stamp2;

    private DomainObject testPerson;
    private String testLogin;

    public static void main(String[] args) {
        try {
            TestStamp test = new TestStamp();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            findOrCreateStamps();
            activateStampedTypes("test_type_44", false);
            activateStampedTypes("test_type_45", false);
            activateStampedTypes("test_type_46", false);
            activateStampedTypes("test_type_47", false);

            // Создаем персону для теста
            testPerson = createTestPerson();
            testLogin = testPerson.getString("login");

            DomainObject testType44 = testOneObject("test_type_44", null);
            DomainObject testType46 = testOneObject("test_type_46", null);
            DomainObject testType47 = testOneObject("test_type_47", null);
            DomainObject testType44_1 = testOneObject("test_type_44_1", testType44);
            DomainObject testType46_1 = testOneObject("test_type_46_1", testType46);
            DomainObject testType47_1 = testOneObject("test_type_47_1", testType47);
            DomainObject testType47_2 = testOneObject("test_type_47_2", testType47);

            if (hasError){
                log("Test filed");
            }else{
                log("Test success");
            }
        } finally {
            writeLog();
        }
    }

    private DomainObject testOneObject(String typeName, DomainObject owner) throws Exception {
        deletePersonStamp(testPerson.getId(), stamp1.getId());
        String query = "select id from " + typeName + "  where id = {0}";

        // Перезачитываем owner
        if (owner != null) {
            owner = getCrudService().find(owner.getId());
        }

        // Создаем грифованный объект
        DomainObject testDomainObject = getCrudService().createDomainObject(typeName);
        testDomainObject.setString("stringField", "_" + System.currentTimeMillis());
        if (owner != null) {
            testDomainObject.setReference(owner.getTypeName(), owner);
        }else {
            testDomainObject.setReference("security_stamp", stamp1);
        }
        testDomainObject = getCrudService().save(testDomainObject);

        // Проверяем, сейчас его должны все видеть, так как пока выключены гррфы
        DomainObject test = getCrudService().find(testDomainObject.getId());
        assertTrue(typeName + " Find by admin, not stamped", test != null );

        IdentifiableObjectCollection testCollection = getCollectionService().findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by admin, not stamped", testCollection.size() > 0);

        test = getCrudService(testLogin).find(testDomainObject.getId());
        assertTrue(typeName + " Find by test person, not stamped", test != null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, not stamped", testCollection.size() > 0);


        // Включаем поддержку грифов
        if (owner == null) {
            activateStampedTypes(typeName, true);
        }else{
            activateStampedTypes(owner.getTypeName(), true);
        }

        // Должны пропасть права у тестового пользователя, у admin остаться
        test = getCrudService().find(testDomainObject.getId());
        assertTrue(typeName + " Find by admin, stamped", test != null );

        testCollection = getCollectionService().findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by admin, stamped", testCollection.size() > 0);

        try {
            test = getCrudService(testLogin).find(testDomainObject.getId());
        }catch (Exception ex){
            // Correct Exception
            test = null;
        }
        assertTrue(typeName + " Find by test person stamped", test == null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person stamped", testCollection.size() == 0);

        // Добавляем тестовому пользователю гриф
        addPersonStamp(testPerson.getId(), stamp1.getId());

        // теперь он должен все видеть
        test = getCrudService(testLogin).find(testDomainObject.getId());
        assertTrue(typeName + " Find by test person with stamp, stamped", test != null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person with stamp, stamped", testCollection.size() > 0);

        // Меняем на документе гриф
        if (owner == null) {
            testDomainObject.setReference("security_stamp", stamp2);
            testDomainObject = getCrudService(testLogin).save(testDomainObject);
        }else{
            owner.setReference("security_stamp", stamp2);
            owner = getCrudService(testLogin).save(owner);
        }

        // Права должны пропасть
        try {
            test = getCrudService(testLogin).find(testDomainObject.getId());
        }catch (Exception ex){
            //Correct exception
            test = null;
        }
        assertTrue(typeName + " Find by test person, stamped, incorrect stamp", test == null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, stamped, incorrect stamp", testCollection.size() == 0);

        // Возвращаем документу гриф, прова должны появится
        // Меняем на документе гриф
        if (owner == null) {
            testDomainObject.setReference("security_stamp", stamp1);
            testDomainObject = getCrudService(testLogin).save(testDomainObject);
        }else{
            owner.setReference("security_stamp", stamp1);
            owner = getCrudService(testLogin).save(owner);
        }

        // Права должны появится
        test = getCrudService(testLogin).find(testDomainObject.getId());
        assertTrue(typeName + " Find by test person, stamped, return correct stamp", test != null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, stamped, return correct stamp", testCollection.size() > 0);

        // Опять меняем гриф на другой, права должны пропасть
        // Меняем на документе гриф
        if (owner == null) {
            testDomainObject.setReference("security_stamp", stamp2);
            testDomainObject = getCrudService(testLogin).save(testDomainObject);
        }else{
            owner.setReference("security_stamp", stamp2);
            owner = getCrudService(testLogin).save(owner);
        }

        // Права должны пропасть
        try {
            test = getCrudService(testLogin).find(testDomainObject.getId());
        }catch (Exception ex){
            //Correct exception
            test = null;
        }
        assertTrue(typeName + " Find by test person, stamped, incorrect stamp 2", test == null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, stamped, incorrect stamp 2", testCollection.size() == 0);

        // Отключаем поддержку прав на тип права должны вернуться
        if (owner == null) {
            activateStampedTypes(typeName, false);
        }else{
            activateStampedTypes(owner.getTypeName(), false);
        }

        test = getCrudService(testLogin).find(testDomainObject.getId());
        assertTrue(typeName + " Find by test person, disable stamp", test != null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, disable stamp", testCollection.size() > 0);

        // Включаем поддержку грифов у типа и удаляем поле гриф у доменного объекта, прова должны остаться
        if (owner == null) {
            activateStampedTypes(typeName, true);
        }else{
            activateStampedTypes(owner.getTypeName(), true);
        }
        if (owner == null) {
            testDomainObject.setReference("security_stamp", (Id)null);
            testDomainObject = getCrudService(testLogin).save(testDomainObject);
        }else{
            owner.setReference("security_stamp", (Id)null);
            owner = getCrudService(testLogin).save(owner);
        }
        test = getCrudService(testLogin).find(testDomainObject.getId());
        assertTrue(typeName + " Find by test person, empty stamp", test != null );

        testCollection = getCollectionService(testLogin).findCollectionByQuery(
                query,
                Arrays.asList(new ReferenceValue(testDomainObject.getId())));
        assertTrue(typeName + " Query by test person, empty stamp 2", testCollection.size() > 0);

        // Возвращаем к исходному
        if (owner == null) {
            testDomainObject.setReference("security_stamp", stamp1);
            testDomainObject = getCrudService(testLogin).save(testDomainObject);
        }else{
            owner.setReference("security_stamp", stamp1);
            owner = getCrudService(testLogin).save(owner);
        }
        if (owner == null) {
            activateStampedTypes(typeName, false);
        }else{
            activateStampedTypes(owner.getTypeName(), false);
        }

        return testDomainObject;
    }

    private void activateStampedTypes(String typeName, boolean activate) throws NamingException {
        DomainObject settings = getCrudService().findByUniqueKey("global_server_settings",
                Collections.singletonMap("name", new StringValue("STAMPED_TYPES")));
        if (settings == null){
            settings = getCrudService().createDomainObject("string_settings");
            settings.setString("name", "STAMPED_TYPES");
        }

        String settingsValue = settings.getString("string_value");
        Set<String> enableStampTypes = new HashSet<>();
        if (settingsValue != null && !settingsValue.isEmpty()){
            String[] setingsValueArray = settingsValue.split("[ ,;]");
            for (int i = 0; i < setingsValueArray.length; i++) {
                enableStampTypes.add(setingsValueArray[i].toLowerCase());
            }
        }

        boolean changed = false;
        if (activate && !enableStampTypes.contains(typeName.toLowerCase())){
            enableStampTypes.add(typeName.toLowerCase());
            changed = true;
        }else if (!activate && enableStampTypes.contains(typeName.toLowerCase())){
            changed = true;
            enableStampTypes.remove(typeName.toLowerCase());
        }

        if (changed){
            settings.setString("string_value", enableStampTypes.stream().collect(Collectors.joining(",")));
            getCrudService().save(settings);
        }
    }

    private void findOrCreateStamps() throws NamingException {
        List<DomainObject> stamps = getCrudService().findAll("security_stamp");
        for (DomainObject stamp : stamps) {
            if (stamp.getString("name").equals(STAMP_1_NAME)){
                stamp1 = stamp;
            }else if (stamp.getString("name").equals(STAMP_2_NAME)){
                stamp2 = stamp;
            }
        }

        if (stamp1 == null){
            stamp1 = createStamp(STAMP_1_NAME);
        }
        if (stamp2 == null){
            stamp2 = createStamp(STAMP_2_NAME);
        }
    }

    private DomainObject createStamp(String name) throws NamingException {
        DomainObject stamp = getCrudService().createDomainObject("security_stamp");
        stamp.setString("name", name);
        return getCrudService().save(stamp);
    }

    private DomainObject createTestPerson() throws NamingException {
        DomainObject testPerson = getCrudService().createDomainObject("person");
        testPerson.setString("login", "person_" + System.currentTimeMillis());
        testPerson.setString("first_name", testPerson.getString("login"));
        testPerson = getCrudService().save(testPerson);

        DomainObject authInfo = getCrudService().createDomainObject("authentication_info");
        authInfo.setString("user_uid", testPerson.getString("login"));
        authInfo.setString("password", TEST_PASSWORD);
        authInfo = getCrudService().save(authInfo);

        return testPerson;
    }

    private void addPersonStamp(Id personId, Id stampId) throws NamingException {
        IdentifiableObjectCollection collection =  getCollectionService().findCollectionByQuery("select id from person_stamp where person = {0} and stamp = {1}",
                Arrays.asList(new ReferenceValue(personId), new ReferenceValue(stampId)));
        if (collection.size() == 0){
            DomainObject personStamp = getCrudService().createDomainObject("person_stamp");
            personStamp.setReference("person", personId);
            personStamp.setReference("stamp", stampId);
            getCrudService().save(personStamp);
        }
    }

    private void deletePersonStamp(Id personId, Id stampId) throws NamingException {
        IdentifiableObjectCollection collection =  getCollectionService().findCollectionByQuery("select id from person_stamp where person = {0} and stamp = {1}",
                Arrays.asList(new ReferenceValue(personId), new ReferenceValue(stampId)));
        for (IdentifiableObject row : collection) {
            getCrudService().delete(row.getId());
        }
    }

    private CrudService.Remote getCrudService() throws NamingException {
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
    }


    private CrudService.Remote getCrudService(String login) throws NamingException {
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class,
                login, TEST_PASSWORD);
    }

    private CollectionsService.Remote getCollectionService() throws NamingException{
        return (CollectionsService.Remote) getService("CollectionsServiceImpl",
                CollectionsService.Remote.class);
    }

    private CollectionsService.Remote getCollectionService(String login) throws NamingException{
        return (CollectionsService.Remote) getService("CollectionsServiceImpl",
                CollectionsService.Remote.class, login, TEST_PASSWORD);
    }
}
