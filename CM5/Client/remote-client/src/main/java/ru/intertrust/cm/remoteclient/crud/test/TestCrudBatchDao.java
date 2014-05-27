package ru.intertrust.cm.remoteclient.crud.test;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCrudBatchDao extends ClientBase {

    private CrudService crudService;


    public static void main(String[] args) {
        try {
            TestCrudBatchDao test = new TestCrudBatchDao();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        crudService = (CrudService) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        List<DomainObject> domainObjects = create();
        update(domainObjects);
        delete(domainObjects);
    }

    private void delete(List<DomainObject> domainObjects) {
        List<Id> ids = new ArrayList<>();
        // check empty
        crudService.delete(ids);

        for (DomainObject domainObject : domainObjects) {
            ids.add(domainObject.getId());
        }
        Collections.reverse(ids);
        int deleted = crudService.delete(ids);

        System.out.println("Objects deleted " + deleted);
    }

    private List<DomainObject> create() {

        List<DomainObject> domainObjects = new ArrayList<>();
        // check empty
        crudService.save(domainObjects);

        DomainObject employee1 = crudService.createDomainObject("Employee");
        employee1.setString("Name", "name1");
        employee1.setString("Position", "admin1");
        employee1.setString("Login", "login1");
        employee1.setString("Email", "e-mail1");
        domainObjects.add(employee1);

        DomainObject employee2 = crudService.createDomainObject("Employee");
        employee2.setString("Name", "name2");
        employee2.setString("Position", "admin2");
        employee2.setString("Login", "login2");
        employee2.setString("Email", "e-mail2");
        domainObjects.add(employee2);

        DomainObject organization = crudService.createDomainObject("Organization");
        organization.setString("Name", "name_org");
        organization.setString("Description", "description");
        domainObjects.add(organization);

        List<DomainObject> saved = crudService.save(domainObjects);

        System.out.println("Objects created");

        return saved;
    }

    private List<DomainObject> update(List<DomainObject> domainObjects) {
        DomainObject employee1 = domainObjects.get(0);
        employee1.setString("Name", "name1_upd");
        employee1.setString("Email", "e-mail1_upd");

        DomainObject employee2 = domainObjects.get(1);
        employee2.setString("Name", "name2_upd");
        employee2.setString("Email", "e-mail2_upd");

        DomainObject organization = domainObjects.get(2);
        organization.setString("Name", "name_org_upd");
        organization.setString("Description", "description_i");
        organization.setReference("Boss", employee1);

        List<DomainObject> saved = crudService.save(domainObjects);

        System.out.println("Objects saved");

        return saved;
    }




}
