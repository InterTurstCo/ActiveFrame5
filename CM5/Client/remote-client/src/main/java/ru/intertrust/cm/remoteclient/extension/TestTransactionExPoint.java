package ru.intertrust.cm.remoteclient.extension;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestTransactionExPoint extends ClientBase {
    
    private CrudService.Remote crudService;
    
    public static void main(String[] args) {
        try {
            TestTransactionExPoint test = new TestTransactionExPoint();
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

          //Создаем
            DomainObject organization = crudService
                    .createDomainObject("Organization");
            String oldName = "Name-" + System.nanoTime();
            String oldDescription = "Description" + System.nanoTime();
            organization.setString("Name", oldName);
            organization.setString("Description", oldDescription);
            //Сохраняем первый раз
            organization = crudService.save(organization);        

            //Меняем
            organization.setString("Name", "Name-" + System.nanoTime());
            organization.setString("Description", "Description" + System.nanoTime());
            //Сохраняем второй раз
            organization = crudService.save(organization);
        } finally {
            writeLog();
        }
    }    
}
