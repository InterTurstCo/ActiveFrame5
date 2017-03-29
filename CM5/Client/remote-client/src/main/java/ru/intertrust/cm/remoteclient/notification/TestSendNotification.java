package ru.intertrust.cm.remoteclient.notification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestSendNotification extends ClientBase {
    private CrudService.Remote crudService;

    private CollectionsService.Remote collectionService;

    private AttachmentService.Remote attachmentService;

    private NotificationService notificationService;

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
                    "RemoteAttachmentServiceImpl", AttachmentService.Remote.class);

            notificationService = (NotificationService.Remote) getService(
                    "NotificationService", NotificationService.Remote.class);

            DomainObject organization = createOrganization("Organization-" + System.currentTimeMillis());

            DomainObject department = createDepartment("Department-" + System.currentTimeMillis(), organization);

            department.setString("Description", "description-" + System.currentTimeMillis());
            department = crudService.save(department);

            DomainObject employee = createEmployee("employee-" + System.currentTimeMillis(), department);

            //Сообщение с несколькими вложениями
            DomainObject test22 = crudService
                    .createDomainObject("test_type_22");
            test22.setString("name", "test-" + System.currentTimeMillis());
            test22 = crudService.save(test22);

            DomainObject firstAttachment = setAttachment(test22, new File("test.pdf"));
            DomainObject secondAttachment = setAttachment(test22, new File("test.bmp"));

            NotificationContext context = new NotificationContext();
            context.addContextObject("attach", test22.getId());

            List<NotificationAddressee> addressee = new ArrayList<NotificationAddressee>();
            addressee.add(new NotificationAddresseePerson(getPersonId("person10")));
            notificationService.sendNow("TEST_MULTY_ATTACH", getPersonId("admin"), 
                    addressee,
                    NotificationPriority.HIGH, context);
            
            //Сообщение с большим subject
            addressee = new ArrayList<NotificationAddressee>();
            addressee.add(new NotificationAddresseePerson(getPersonId("person10")));
            notificationService.sendNow("TEST_LONG_SUBJECT", getPersonId("admin"), 
                    addressee,
                    NotificationPriority.HIGH, context);
            
            
            System.out.println("Test End");
        } finally {
            writeLog();
        }
    }

    private Id getPersonId(String personLogin) throws NamingException {
        IdentifiableObjectCollection collection =
                collectionService.findCollectionByQuery("select t.id from person t where t.login = '" + personLogin
                        + "'");
        Id result = null;
        if (collection.size() > 0) {
            result = collection.getId(0);
        }
        return result;
    }

    private DomainObject setAttachment(DomainObject domainObject, File file) throws IOException {
        return setAttachment(domainObject, file.getName(), readFile(file));
    }

    private DomainObject setAttachment(DomainObject domainObject, String name, byte[] content) throws IOException {
        DomainObject attachment =
                attachmentService.createAttachmentDomainObjectFor(domainObject.getId(),
                        "test_type_22_attach");
        attachment.setString("Name", name);
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(bis);

        RemoteInputStream remoteInputStream;
        remoteInputStream = simpleRemoteInputStream.export();
        DomainObject result = attachmentService.saveAttachment(remoteInputStream, attachment);
        return result;
    }

    private DomainObject createEmployee(String name, DomainObject department) {
        DomainObject employee = findDomainObject("employee_test", "Name", name);
        if (employee == null) {
            employee = crudService
                    .createDomainObject("employee_test");
            employee.setString("Name", name);
            employee.setString("Login", name);
            employee.setString("FirstName", name);
            employee.setString("Position", "Ген. Дир");
            employee.setReference("Department", department);
            employee = crudService.save(employee);
            log("Создан объект " + employee.getTypeName() + " " + employee.getId());
        }
        return employee;
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
