package ru.intertrust.cm.remoteclient.permissions.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission.Permission;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestPermission extends ClientBase {

    private CrudService.Remote crudService;
    private CollectionsService.Remote collectionService;
    private PermissionService.Remote permissionService;

    public static void main(String[] args) {
        try {
            TestPermission test = new TestPermission();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
            collectionService =
                    (CollectionsService.Remote) getService("CollectionsServiceImpl", CollectionsService.Remote.class);
            permissionService = (PermissionService.Remote) getService("PermissionService",
                    PermissionService.Remote.class);

            //Создаем внутренний документ
            DomainObject internalDocument = crudService.createDomainObject("Internal_Document");
            internalDocument.setString("Name", "Тестовый документ " + System.nanoTime());
            internalDocument.setString("ReturnOnReject", "YES");
            internalDocument.setLong("Stage", 0L);
            internalDocument.setString("RegNum", "InternalDoc111");
            internalDocument.setReference("docAuthor", getEmployeeId("Employee-1"));
            internalDocument.setReference("Registrant", getEmployeeId("Employee-4"));
            internalDocument = crudService.save(internalDocument);

            //Создание карточек согласования
            for (int i = 0; i < 2; i++) {
                createNegotiationCard(internalDocument.getId(), "Employee-" + (i + 2));
            }

            //Проверка прав
            EtalonPermissions etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Employee-1"), Permission.Delete);
            etalon.addActionPermission(getEmployeeId("Employee-1"), "StartProcessAction");
            etalon.addActionPermission(getEmployeeId("Employee-1"), "ChangeStatusAction");
            checkPermissions(internalDocument.getId(), etalon, "Status Draft");

            //Смена статуса + проверка прав. Статус сейчас меняется в строковом поле, после в точке расширения отлавливается это изменение 
            //и меняется статус уже с помощью метода setState. Это сделано для тестирования и невозможности сменить статус снаружи
            internalDocument.setString("State", "Negotiation");
            internalDocument = crudService.save(internalDocument);
            internalDocument = crudService.find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Employee-1"), Permission.Write);
            etalon.addPermission(getEmployeeId("Employee-2"), Permission.Read);
            etalon.addPermission(getEmployeeId("Employee-3"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Status Negotiation");

            //Добавляем еще согласующего, права должны пересчитаться
            createNegotiationCard(internalDocument.getId(), "Employee-5");
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Employee-1"), Permission.Write);
            etalon.addPermission(getEmployeeId("Employee-2"), Permission.Read);
            etalon.addPermission(getEmployeeId("Employee-3"), Permission.Read);
            etalon.addPermission(getEmployeeId("Employee-5"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Add new Negotiator");

            internalDocument.setString("State", "Registration");
            internalDocument = crudService.save(internalDocument);
            internalDocument = crudService.find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Employee-1"), Permission.Write);
            etalon.addPermission(getEmployeeId("Employee-4"), Permission.Write);
            etalon.addPermission(getEmployeeId("Employee-2"), Permission.Read);
            etalon.addPermission(getEmployeeId("Employee-3"), Permission.Read);
            etalon.addPermission(getEmployeeId("Employee-5"), Permission.Read);
            checkPermissions(internalDocument.getId(), etalon, "Status Registration");

            internalDocument.setString("State", "Registred");
            internalDocument = crudService.save(internalDocument);
            internalDocument = crudService.find(internalDocument.getId());
            etalon = new EtalonPermissions();
            checkPermissions(internalDocument.getId(), etalon, "Status Registred");

            internalDocument.setString("State", "OnRevision");
            internalDocument = crudService.save(internalDocument);
            internalDocument = crudService.find(internalDocument.getId());
            etalon = new EtalonPermissions();
            etalon.addPermission(getEmployeeId("Employee-1"), Permission.Write);
            checkPermissions(internalDocument.getId(), etalon, "Status OnRevision");

            log("Test complete");
        } finally {
            writeLog();
        }
    }

    private void checkPermissions(Id domainObjectId, EtalonPermissions etalon, String massage) {
        List<DomainObjectPermission> serverPermission = permissionService.getObjectPermissions(domainObjectId);
        etalon.compare(serverPermission, massage);
    }

    private Id getEmployeeId(String employeeName) {
        IdentifiableObjectCollection collection =
                collectionService.findCollectionByQuery("select t.id from Employee t where t.Name = '" + employeeName
                        + "'");
        Id result = null;
        if (collection.size() > 0) {
            result = collection.getId(0);
        }
        return result;
    }

    private void createNegotiationCard(Id documentId, String employeeName) {
        DomainObject negotiationCard = crudService.createDomainObject("Negotiation_Card");
        negotiationCard.setString("Name", "карточка согласующего");
        negotiationCard.setReference("Parent_Document", documentId);
        negotiationCard.setReference("Negotiator", getEmployeeId(employeeName));
        negotiationCard = crudService.save(negotiationCard);

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

        public boolean compare(List<DomainObjectPermission> serverPermission, String massage) {
            boolean result = true;

            if (serverPermission.size() != personPermission.size()){
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

            if (result){
                log(massage + " Compare OK");
            }else{
                log(massage + " Compare ERROR");
            }
            
            return result;
        }
    }
}
