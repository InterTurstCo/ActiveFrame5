package ru.intertrust.cm.remoteclient.permissions.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import junit.framework.Assert;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission.Permission;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestPermission extends ClientBase {

    public static void main(String[] args) {
        try {
            TestPermission test = new TestPermission();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CrudService.Remote getCrudService() throws NamingException{
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private CollectionsService.Remote getCollectionService() throws NamingException{
        return (CollectionsService.Remote) getService("CollectionsServiceImpl", CollectionsService.Remote.class);
    }
    
    private PermissionService.Remote getPermissionService() throws NamingException{
        return (PermissionService.Remote) getService("PermissionService",
                PermissionService.Remote.class);
    }
    
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            //Создаем внутренний документ
            DomainObject internalDocument = getCrudService().createDomainObject("Internal_Document");
            internalDocument.setString("Name", "Тестовый документ " + System.nanoTime());
            internalDocument.setString("ReturnOnReject", "YES");
            internalDocument.setLong("Stage", 0L);
            internalDocument.setString("RegNum", "InternalDoc111");
            internalDocument.setReference("docAuthor", getEmployeeId("Сотрудник 3"));
            internalDocument.setReference("Registrant", getEmployeeId("Сотрудник 4"));
            internalDocument = getCrudService().save(internalDocument);

            List<Id> negotiationCards = new ArrayList<Id>();
            
            //Создание карточек согласования
            for (int i = 0; i < 2; i++) {
                DomainObject negotiation = createNegotiationCard(internalDocument.getId(), "Сотрудник " + (i + 5));
                negotiationCards.add(negotiation.getId());
            }
                        
            //Проверка прав
            EtalonPermissions etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Delete);
            etalon.addPermission(getPersonId("admin"), Permission.Delete);
            etalon.addPermission(getEmployeeId("Сотрудник 1"), Permission.Delete);
            etalon.addActionPermission(getEmployeeId("Сотрудник 3"), "StartProcessAction");
            etalon.addActionPermission(getEmployeeId("Сотрудник 3"), "ChangeStatusAction");
            checkPermissions(internalDocument.getId(), etalon, "Status Draft");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Status Draft");
            }

            //Смена статуса + проверка прав. Статус сейчас меняется в строковом поле, после в точке расширения отлавливается это изменение 
            //и меняется статус уже с помощью метода setState. Это сделано для тестирования и невозможности сменить статус снаружи
            internalDocument.setString("State", "Negotiation");
            internalDocument = getCrudService().save(internalDocument);
            internalDocument = getCrudService().find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Write);
            etalon.addPermission(getEmployeeId("Сотрудник 5"), Permission.Read);
            etalon.addPermission(getEmployeeId("Сотрудник 6"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Status Negotiation");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Status Negotiation");
            }            

            //Добавляем еще согласующего, права должны пересчитаться
            createNegotiationCard(internalDocument.getId(), "Сотрудник 7");
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Write);
            etalon.addPermission(getEmployeeId("Сотрудник 5"), Permission.Read);
            etalon.addPermission(getEmployeeId("Сотрудник 6"), Permission.Read);
            etalon.addPermission(getEmployeeId("Сотрудник 7"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Add new Negotiator");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Add new Negotiator");
            }            

            internalDocument.setString("State", "Registration");
            internalDocument = getCrudService().save(internalDocument);
            internalDocument = getCrudService().find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Write);
            etalon.addPermission(getEmployeeId("Сотрудник 4"), Permission.Write);
            etalon.addPermission(getEmployeeId("Сотрудник 5"), Permission.Read);
            etalon.addPermission(getEmployeeId("Сотрудник 6"), Permission.Read);
            etalon.addPermission(getEmployeeId("Сотрудник 7"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Status Registration");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Status Registration");
            }            
            //Сотрудником 3 редактируем документ и карточку согласования
            changeObject(internalDocument.getId(), getEmployee("Сотрудник 3").getString("login"), "Name", "Тестовый документ " + System.nanoTime());
            internalDocument = getCrudService().find(internalDocument.getId());
            changeObject(negotiationCards.get(0), getEmployee("Сотрудник 3").getString("login"), "Name", "Карточка согласующего " + System.nanoTime());
            
            //Пытаемся удалить под сотрудником 5 карточку согласования, должны получить ошибку
            try{
                deleteObject(negotiationCards.get(0), getEmployee("Сотрудник 7").getString("login"));
                Assert.assertTrue("Не должно быть прав на удаление", false);
            }catch(Exception ignoreEx){
            }

            //Пытаемся удалить под пользователем 3 должно удалится, так как должен отработать мапинг прав
            deleteObject(negotiationCards.get(0), getEmployee("Сотрудник 3").getString("login"));
            negotiationCards.remove(0);
            
            internalDocument.setString("State", "Registred");
            internalDocument = getCrudService().save(internalDocument);
            internalDocument = getCrudService().find(internalDocument.getId());
            etalon = new EtalonPermissions();
            //В этом статусе право read имеют все пользователи
            List<Id> allPersons = getAllPersons();
            for (Id personId : allPersons) {
                etalon.addPermission(personId, Permission.Read);
            }
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Delete);
            checkPermissions(internalDocument.getId(), etalon, "Status Registred");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Status Registred");
            }
            
            //Удаляем карточку согласования. Должно удалится без ошибок
            deleteObject(negotiationCards.get(0), getEmployee("Сотрудник 3").getString("login"));
            negotiationCards.remove(0);

            internalDocument.setString("State", "OnRevision");
            internalDocument = getCrudService().save(internalDocument);
            internalDocument = getCrudService().find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Write);
            checkPermissions(internalDocument.getId(), etalon, "Status OnRevision");
            for (Id negotiationId  : negotiationCards) {
                checkPermissions(negotiationId, etalon, "Status OnRevision");
            }            

            //Создаем письмо
            DomainObject letter = getCrudService().createDomainObject("letter");
            letter.setString("subject", "Тестовое письмо " + System.nanoTime());
            letter = getCrudService().save(letter);

            etalon = new EtalonPermissions();
            etalon.addPermission(getPersonId("admin"), Permission.Delete);
            etalon.addPermission(getEmployeeId("Сотрудник 1"), Permission.Delete);
            etalon.addPermission(getEmployeeId("Сотрудник 2"), Permission.Read);
            etalon.addActionPermission(getEmployeeId("Сотрудник 1"), "action1");
            etalon.addActionPermission(getPersonId("admin"), "action1");
            checkPermissions(letter.getId(), etalon, "New letter");

            //Проверяем косвенные права с учетом наследования объектов у которых заимствуются права
            DomainObject testOutgoingDocument = getCrudService().createDomainObject("test_outgoing_document");
            testOutgoingDocument.setString("name", "Тестовый документ " + System.nanoTime());
            testOutgoingDocument.setReference("author", getEmployee("Сотрудник 2"));
            testOutgoingDocument.setReference("signer", getEmployee("Сотрудник 3"));
            testOutgoingDocument = getCrudService().save(testOutgoingDocument);
            
            DomainObject testResolution = getCrudService().createDomainObject("test_resolution");
            testResolution.setString("name", "Тестовый документ " + System.nanoTime());
            testResolution.setReference("executor", getEmployee("Сотрудник 4"));
            testResolution.setReference("document", testOutgoingDocument);
            testResolution = getCrudService().save(testResolution);

            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Сотрудник 2"), Permission.Delete );
            etalon.addPermission(getEmployeeId("Сотрудник 3"), Permission.Delete);
            checkPermissions(testOutgoingDocument.getId(), etalon, "test_outgoing_document");
            checkPermissions(testResolution.getId(), etalon, "test_resolution");
            
            //Проверка косвенных прав на "абстрактные" типы
            DomainObject testOrganization = getCrudService().createDomainObject("organization_test");
            testOrganization.setString("Name", "Name " + System.nanoTime());
            testOrganization = getCrudService().save(testOrganization);
            
            DomainObject testDepartment = getCrudService().createDomainObject("department_test");
            testDepartment.setString("name", "Name " + System.nanoTime());
            testDepartment.setReference("Organization", testOrganization);
            testDepartment = getCrudService().save(testDepartment);
            
            DomainObject testEmployee = getCrudService().createDomainObject("employee_test");
            testEmployee.setString("name", "Name " + System.nanoTime());
            testEmployee.setString("login", testEmployee.getString("name"));
            testEmployee.setString("Position", "Boss");
            testEmployee.setReference("Department", testDepartment);
            testEmployee = getCrudService().save(testEmployee);

            etalon = new EtalonPermissions();
            for (Id personId : allPersons) {
                etalon.addPermission(personId, Permission.Delete);
                etalon.addActionPermission(personId, "action1");
            }            
            checkPermissions(testEmployee.getId(), etalon, "test_employee");
            
            CollectionsService notAdminCollectionService = (CollectionsService)getService("CollectionsServiceImpl", CollectionsService.Remote.class, "person1", "admin");
            IdentifiableObjectCollection collection = notAdminCollectionService.findCollectionByQuery("select * from person_test");
            assertTrue("test employee query", collection.size() > 0);
            
            //Проверка косвенных прав, ссылающихся на матрицу с read everybody
            DomainObject testType1 = getCrudService().createDomainObject("test_type_1");
            testType1.setString("name", "Name " + System.nanoTime());
            testType1 = getCrudService().save(testType1);
            
            DomainObject testType2 = getCrudService().createDomainObject("test_type_2");
            testType2.setString("name", "Name " + System.nanoTime());
            testType2.setReference("test_type_1", testType1);
            testType2 = getCrudService().save(testType2);
            
            collection = notAdminCollectionService.findCollectionByQuery("select * from test_type_1");
            assertTrue("test test_type_1 query", collection.size() > 0);

            collection = notAdminCollectionService.findCollectionByQuery("select * from test_type_2");
            assertTrue("test test_type_2 query", collection.size() > 0);
            
            //Проверка прав на родительский тип в случае матрицы у дочернего типа CMFIVE-1494
            /*DomainObject testType4 = getCrudService().createDomainObject("test_type_4");
            testType4.setString("name", "Name " + System.nanoTime());
            testType4.setReference("employee", getEmployeeId("Сотрудник 5"));
            testType4 = getCrudService().save(testType4);
            
            notAdminCollectionService = (CollectionsService)getService("CollectionsServiceImpl", CollectionsService.Remote.class, "person5", "admin");
            String query = "select id, created_date from (select t4.id, t3.created_date, t3.status, t4.employee from test_type_4 t4 left join test_type_3 t3 on t3.id = t4.id where t4.id = {0}) t";
            List<Value> params = new ArrayList<Value>();
            params.add(new ReferenceValue(testType4.getId()));
            collection = notAdminCollectionService.findCollectionByQuery(query, params);
            assertTrue("test test_type_4 query", collection.size() > 0 && collection.get(0).getTimestamp("created_date") != null);
            
            notAdminCollectionService = (CollectionsService)getService("CollectionsServiceImpl", CollectionsService.Remote.class, "person6", "admin");
            collection = notAdminCollectionService.findCollectionByQuery(query, params);
            assertTrue("test test_type_4 query without permissions", collection.size() == 0);*/
            
            //Проверка прав на дочерний тип в случае матрицы у родительского типа CMFIVE-1541
            DomainObject testType6 = getCrudService().createDomainObject("test_type_6");
            testType6.setString("name", "Name " + System.nanoTime());
            testType6.setString("description", "Description " + System.nanoTime());
            Id employee5Id = getEmployeeId("Сотрудник 5");
            testType6.setReference("employee", employee5Id);
            testType6 = getCrudService().save(testType6);
            
            CrudService notAdminCrudservice = (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class, "person6", "admin");
            List<DomainObject> linkedObjects = notAdminCrudservice.findLinkedDomainObjects(employee5Id, "test_type_5", "employee");
            assertTrue("test_type_6 linked object count", linkedObjects.size() > 0);
            assertTrue("tes_type_6 name field", linkedObjects.get(0) != null);
            
            log("Test complete");
        } finally {
            writeLog();
        }
    }

    private void deleteObject(Id objectId, String login) throws NamingException {
        CrudService.Remote localCrudService = (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class, login, "admin"); 
        localCrudService.delete(objectId);
    }

    private DomainObject changeObject(Id objectId, String login, String field, String value) throws NamingException {
        CrudService.Remote localCrudService = (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class, login, "admin"); 
        DomainObject domainObject = localCrudService.find(objectId);
        domainObject.setString(field, value);
        domainObject = localCrudService.save(domainObject);
        return domainObject;
    }

    private List<Id> getAllPersons() throws NamingException {
        List<Id> result = new ArrayList<Id>();
        IdentifiableObjectCollection collection =
                getCollectionService().findCollectionByQuery("select id from person");
        for (IdentifiableObject identifiableObject : collection) {
            result.add(identifiableObject.getId());
        }
        return result;
    }

    private void checkPermissions(Id domainObjectId, EtalonPermissions etalon, String massage) throws Exception {
        List<DomainObjectPermission> serverPermission = getPermissionService().getObjectPermissions(domainObjectId);
        etalon.compare(serverPermission, massage);
    }

    private Id getEmployeeId(String employeeName) throws NamingException {
        IdentifiableObjectCollection collection =
                getCollectionService().findCollectionByQuery("select t.id from Employee t where t.Name = '" + employeeName
                        + "'");
        Id result = null;
        if (collection.size() > 0) {
            result = collection.getId(0);
        }
        return result;
    }

    private DomainObject getEmployee(String employeeName) throws NamingException {
        DomainObject result = null;
        IdentifiableObjectCollection collection =
                getCollectionService().findCollectionByQuery("select t.id from Employee t where t.Name = '" + employeeName
                        + "'");
        if (collection.size() > 0) {
            result = getCrudService().find(collection.getId(0));
        }
        return result;
    }    
    
    private Id getPersonId(String personLogin) throws NamingException {
        IdentifiableObjectCollection collection =
                getCollectionService().findCollectionByQuery("select t.id from person t where t.login = '" + personLogin
                        + "'");
        Id result = null;
        if (collection.size() > 0) {
            result = collection.getId(0);
        }
        return result;
    }

    private DomainObject createNegotiationCard(Id documentId, String employeeName) throws NamingException {
        DomainObject negotiationCard = getCrudService().createDomainObject("Negotiation_Card");
        negotiationCard.setString("Name", "карточка согласующего");
        negotiationCard.setReference("Parent_Document", documentId);
        negotiationCard.setReference("Negotiator", getEmployeeId(employeeName));
        negotiationCard = getCrudService().save(negotiationCard);
        return negotiationCard;
    }

    private class EtalonPermissions {
        private Map<Id, Permission> personPermission = new HashMap<Id, Permission>();
        private Map<Id, List<String>> personActionPermission = new HashMap<Id, List<String>>();

        public void addPermission(Id personId, Permission permission) {
            personPermission.put(personId, permission);
        }

        public void addActionPermission(Id personId, String action) {
            List<String> actions = personActionPermission.get(personId);
            if (actions == null) {
                actions = new ArrayList<String>();
                personActionPermission.put(personId, actions);
            }
            actions.add(action);
        }

        public boolean compare(List<DomainObjectPermission> serverPermission, String massage) throws Exception {
            boolean result = true;

            if (serverPermission.size() != personPermission.size()) {
                log("ACL NOT equals: ACL entry count not equals");
                result = false;
            }

            for (DomainObjectPermission domainObjectPermission : serverPermission) {
                if (!domainObjectPermission.getPermission().equals(
                        personPermission.get(domainObjectPermission.getPersonId()))) {
                    log("ACL NOT equals: person id = " + domainObjectPermission.getPersonId() + " need "
                            + personPermission.get(domainObjectPermission.getPersonId()) + " base "
                            + domainObjectPermission.getPermission());
                    result = false;
                }

                List<String> etalonActions = personActionPermission.get(domainObjectPermission.getPersonId());

                for (String action : domainObjectPermission.getActions()) {
                    if (etalonActions == null || !etalonActions.contains(action)) {
                        log("ACL NOT equals: person id = " + domainObjectPermission.getPersonId() + " contain "
                                + action + " but etalon not contain it");
                        result = false;
                    }
                }

                if (etalonActions != null) {
                    for (String action : etalonActions) {
                        if (!domainObjectPermission.getActions().contains(action)) {
                            log("ACL NOT equals: person id = " + domainObjectPermission.getPersonId() + " contain "
                                    + action + " but in base not contain it");
                            result = false;
                        }
                    }
                }

            }

            if (result) {
                log(massage + " Compare OK");
            } else {
                log(massage + " Compare ERROR");
            }

            assertTrue(massage, result);
            return result;
        }
    }
}
