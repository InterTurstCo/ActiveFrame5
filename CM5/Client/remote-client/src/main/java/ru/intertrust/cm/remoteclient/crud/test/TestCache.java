package ru.intertrust.cm.remoteclient.crud.test;

import java.util.Date;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
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
        crudService = (CrudService) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        //Проверка кэша
        for (int i = 0; i < 20; i++) {
            
            DomainObject domainObject = create();
            domainObject = crudService.save(domainObject);
            
            domainObject = crudService.find(domainObject.getId());           
            domainObject.setString("name", "Test2");
            domainObject = crudService.save(domainObject);                
        }
        System.out.println("Test OptimisticLockException OK");
        
        //проверка дат
        DomainObject domainObject = create();
        domainObject.setDateTimeWithTimeZone("reg_date", new DateTimeWithTimeZone(new Date(), TimeZone.getDefault()));
        domainObject = crudService.save(domainObject);
        
        domainObject = crudService.find(domainObject.getId());
        assertTrue("Tate time", domainObject.getValue("reg_date") instanceof DateTimeValue);
        System.out.println("Test DateTime OK");
                
        System.out.println("Test complete");
        
    }


    private DomainObject create() {

        DomainObject domainObject = crudService.createDomainObject("test_type_16");
        domainObject.setString("name", "Test");
        domainObject.setString("status_name", "Черновик");
        
        System.out.println("Objects created");

        return domainObject;
    }


}
