package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class CreateTestData
        extends ClientBase {
    private CrudService.Remote crudService;
    private CollectionsService.Remote collectionService;

    public static void main(String[] args) {
        try {
            CreateTestData test = new CreateTestData();
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
            
            ProcessService.Remote processService = (ProcessService.Remote) getService(
                    "ProcessService", ProcessService.Remote.class);            

            
            
            String[] statuses = new String[] { "Active","Draft","Active4","Active3",
                    "Negotiation","Agree","Sign","Registration",
                    "Registred", "AdditionalNegotiation", "OnRevision", 
                    "Send", "Acquired", "Complete", "Pretermit", "Sleep", "Ready", "Wait", "Run"};
            
            for (String status : statuses) {
                createStatus(status);
            }
            
            createPerson("admin");

            DomainObject organization = createOrganization("Organization-1");
            DomainObject department = createDepartment("Department-1", organization);

            for (int i = 1; i < 11; i++) {
                createEmployee("Employee-" + i, "user" + i, department);
            }

            
            byte[] processDef = getProcessAsByteArray("templates/testInternalDoc/InternalDoc.bpmn");
            Id defId = processService.saveProcess(processDef,
                    "InternalDoc.bpmn", true);

            processDef = getProcessAsByteArray("templates/testInternalDoc/Negotiation.bpmn");
            defId = processService.saveProcess(processDef,
                    "Negotiation.bpmn", true);
            
            processDef = getProcessAsByteArray("templates/testInternalDoc/Registration.bpmn");
            defId = processService.saveProcess(processDef,
                    "Registration.bpmn", true);
            
            processDef = getProcessAsByteArray("templates/testInternalDoc/DocExecution.bpmn");
            defId = processService.saveProcess(processDef,
                    "DocExecution.bpmn", true);
            
            processDef = getProcessAsByteArray("templates/testInternalDoc/CommissionExecution.bpmn");
            defId = processService.saveProcess(processDef,
                    "CommissionExecution.bpmn", true);
        } finally {
            writeLog();
        }
    }

    private DomainObject createStatus(String statusName) {
        DomainObject status = findDomainObject("Status", "Name", statusName);
        if (status == null) {
            status = crudService
                    .createDomainObject("Status");
            status.setString("Name", statusName);
            status = crudService.save(status);
            log("Создан объект " + status.getTypeName() + " " + status.getId());
        }
        return status;
    }

    private DomainObject createEmployee(String name, String login, DomainObject department) throws IOException {
        DomainObject employee = findDomainObject("Employee", "Name", name);
        if (employee == null) {
            employee = crudService
                    .createDomainObject("Employee");
            employee.setReference("Department", department);
            employee.setString("Name", name);
            employee.setString("Position", "Должность 1");
            employee.setString("Phone", "+7-" + System.nanoTime());
            employee.setString("Login", login);
            employee = crudService.save(employee);
            log("Создан объект " + employee.getTypeName() + " " + employee.getId());
        }

        DomainObject authInfo = findDomainObject("Authentication_Info", "User_Uid", login);
        if (authInfo == null) {
            authInfo = crudService
                    .createDomainObject("Authentication_Info");
            authInfo.setString("User_Uid", login);
            String psswd = getAppPropery("person.password");
            authInfo.setString("Password", psswd);
            authInfo = crudService.save(authInfo);
            log("Создан объект " + authInfo.getTypeName() + " " + authInfo.getId());
        }

        return employee;
    }

    private DomainObject createPerson(String login) throws IOException {
        DomainObject person = findDomainObject("Person", "Login", login);
        if (person == null) {
            person = crudService
                    .createDomainObject("Person");
            person.setString("Login", login);
            person = crudService.save(person);
            log("Создан объект " + person.getTypeName() + " " + person.getId());
        }

        DomainObject authInfo = findDomainObject("Authentication_Info", "User_Uid", login);
        if (authInfo == null) {
            authInfo = crudService
                    .createDomainObject("Authentication_Info");
            authInfo.setString("User_Uid", login);
            String psswd = getAppPropery("admin.password");
            authInfo.setString("Password", psswd);
            authInfo = crudService.save(authInfo);
            log("Создан объект " + authInfo.getTypeName() + " " + authInfo.getId());
        }

        return person;
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

    private DomainObject createDepartment(String name, DomainObject organization) {
        DomainObject department = findDomainObject("Department", "Name", name);
        if (department == null) {
            department = crudService
                    .createDomainObject("Department");
            department.setString("Name", name);
            department.setReference("Organization", organization);
            department = crudService.save(department);
            log("Создан объект " + department.getTypeName() + " " + department.getId());
        }
        return department;
    }

    private DomainObject createOrganization(String name) {
        DomainObject organization = findDomainObject("Organization", "Name", name);
        if (organization == null) {
            organization = crudService
                    .createDomainObject("Organization");
            organization.setString("Name", name);
            organization = crudService.save(organization);
            log("Создан объект " + organization.getTypeName() + " " + organization.getId());
        }
        return organization;
    }
    
    private byte[] getProcessAsByteArray(String processPath) throws IOException {
        FileInputStream stream = null;
        ByteArrayOutputStream out = null;
        try {
            stream = new FileInputStream(processPath);
            out = new ByteArrayOutputStream();

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = stream.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            return out.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }    
}
