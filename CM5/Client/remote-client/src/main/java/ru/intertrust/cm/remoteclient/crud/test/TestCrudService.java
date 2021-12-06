package ru.intertrust.cm.remoteclient.crud.test;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCrudService extends ClientBase {

    private CrudService crudService;


    public static void main(String[] args) {
        try {
            TestCrudService test = new TestCrudService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        crudService = (CrudService) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        DomainObject domainObject = create();
        domainObject = crudService.save(domainObject);        
        crudService.delete(domainObject.getId());
        System.out.println("Delete one OK");
        
        List<Id> ids = new ArrayList<Id>();
        ids.add(domainObject.getId());
        ids.add(domainObject.getId());
        //Не должно быть ошибки
        crudService.delete(ids);
        System.out.println("Delete many not found OK");
        
        //Должна быть ошибка
        boolean objectNotFound = false;
        try{
            crudService.delete(domainObject.getId());
        }catch(ObjectNotFoundException ignoreEx){
            objectNotFound = true;
            System.out.println("Delete one not found OK");
        }
        
        if (!objectNotFound){
            throw new Exception("Need throw ObjectNotFound");
        }
        
        //Проверяем исключение при превышении длины поля
        DomainObject testType31 = crudService.createDomainObject("test_type_31");
        testType31.setString("name", "123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_");
        try{
            crudService.save(testType31);
        }catch(Exception ex){
            System.out.println("Correct error " + ex.getMessage());
        }

        //Проверяем исключение при пустом not-null поле
        testType31 = crudService.createDomainObject("test_type_31");
        try{
            crudService.save(testType31);
        }catch(Exception ex){
            System.out.println("Correct error " + ex.getMessage());
        }

        // Проверяем исключение при поиске несуществующего типа
        try{
            crudService.find(new RdbmsId(9999, 111));
        }catch(ObjectNotFoundException ignoreEx){
            objectNotFound = true;
            System.out.println("Find incorrect type OK");
        }

        if (!objectNotFound){
            throw new Exception("Need throw ObjectNotFound");
        }

        if (hasError) {
            System.out.println("Test ERROR");
        }else {
            System.out.println("Test complete");
        }
    }


    private DomainObject create() {

        DomainObject employee1 = crudService.createDomainObject("Employee");
        employee1.setString("Name", "name1");
        employee1.setString("Position", "admin1");
        employee1.setString("Login", "login" + System.currentTimeMillis());
        employee1.setString("Email", "e-mail" + System.currentTimeMillis());
        System.out.println("Objects created");

        return employee1;
    }


}
