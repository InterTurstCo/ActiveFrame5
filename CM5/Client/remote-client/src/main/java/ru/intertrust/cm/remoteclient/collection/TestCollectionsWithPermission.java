package ru.intertrust.cm.remoteclient.collection;

import java.util.List;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;

public class TestCollectionsWithPermission extends TestCollection{
    public static void main(String[] args) {
        try {
            TestCollectionsWithPermission test = new TestCollectionsWithPermission();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "person10", "admin");

        crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class, "person10", "admin");

        String query = "select t.id, t.login from person_test t";
        executeQuery(query, 2);

        query = "select t.id, t.name from employee_test t";
        executeQuery(query, 2);
        
        query = "select t.id, t.name, p.login from employee_test t inner join person_test p on (p.id = t.id)";
        executeQuery(query, 3);
        
        query = "select t.id, t.signer from test_outgoing_document t";
        executeQuery(query, 2);
        
        List<DomainObject> organizations = crudService.findAll("organization_test");
        
        for (DomainObject organization : organizations) {
            System.out.println("Find organization \""  + organization.getString("name") + "\"");
            List<DomainObject> departments = crudService.findLinkedDomainObjects(organization.getId(), "department_test", "Organization");
            for (DomainObject department : departments) {
                System.out.println("Find department \""  + department.getString("name") + "\"");
                List<DomainObject> employees = crudService.findLinkedDomainObjects(department.getId(), "employee_test", "Department");
                for (DomainObject employee : employees) {
                    System.out.println("Find employee \""  + employee.getString("name") + "\"");
                }
            }
        }
        
    }
}
