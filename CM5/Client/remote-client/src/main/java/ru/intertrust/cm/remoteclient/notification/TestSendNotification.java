package ru.intertrust.cm.remoteclient.notification;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestSendNotification extends ClientBase {
    private CrudService.Remote crudService;

    private CollectionsService.Remote collectionService;

    private AttachmentService.Remote attachmentService;

    public static void main(String[] args) {
        try {
            TestSendNotification test = new TestSendNotification();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            collectionService = (CollectionsService.Remote) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);

            attachmentService = (AttachmentService.Remote) getService(
                    "AttachmentServiceImpl", AttachmentService.Remote.class);

            DomainObject organization = createOrganization("Organization-" + System.currentTimeMillis());

            DomainObject department = createDepartment("Department-" + System.currentTimeMillis(), organization);

            department.setString("Description", "description-" + System.currentTimeMillis());
            department = crudService.save(department);

            System.out.println("Test End");
        } finally {
            writeLog();
        }
    }

    private DomainObject createOrganization(String name) {
        DomainObject organization = findDomainObject("organization_test", "Name", name);
        if (organization == null) {
            organization = crudService
                    .createDomainObject("organization_test");
            organization.setString("Name", name);
            organization = crudService.save(organization);
            log("Создан объект " + organization.getTypeName() + " " + organization.getId());
        }
        return organization;
    }

    private DomainObject createDepartment(String name, DomainObject organization) {
        DomainObject department = findDomainObject("department_test", "Name", name);
        if (department == null) {
            department = crudService
                    .createDomainObject("department_test");
            department.setString("Name", name);
            department.setReference("Organization", organization);
            department = crudService.save(department);
            log("Создан объект " + department.getTypeName() + " " + department.getId());
        }
        return department;
    }

    private DomainObject findDomainObject(String type, String field, String fieldValue) {
        String query = "select t.id from " + type + " t where t." + field + "='" + fieldValue + "'";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        DomainObject result = null;
        if (collection.size() > 0) {
            result = crudService.find(collection.get(0).getId());
            log("Найден объект " + result.getTypeName() + " " + result.getId());
        }
        return result;
    }
}
