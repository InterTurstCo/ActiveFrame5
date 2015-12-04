package ru.intertrust.cm.remoteclient.crud.test;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCache extends ClientBase{
    private CrudService crudService;


    public static void main(String[] args) {
        try {
            TestCache test = new TestCache();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        for (int i = 0; i < 20; i++) {
            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);
            
            //Тест глобального кэша под разными пользователями
            DomainObject domainObject = create();
            domainObject = crudService.save(domainObject);
            
            domainObject = crudService.find(domainObject.getId());
            domainObject.setString("Position", "admin2");
            domainObject = crudService.save(domainObject);                
        }
        System.out.println("Test complete");
        
    }


    private DomainObject create() {

        DomainObject employee1 = crudService.createDomainObject("test_type_16");
        employee1.setString("status_name", "Черновик");
        System.out.println("Objects created");

        return employee1;
    }


}
