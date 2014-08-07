package ru.intertrust.cm.remoteclient.extension;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestExtPointOnTypeHierarchy extends ClientBase{
    private String suffix = String.valueOf(System.currentTimeMillis());
    
    public static void main(String[] args) {
        try {
            TestExtPointOnTypeHierarchy test = new TestExtPointOnTypeHierarchy();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            CrudService crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            
            DomainObject employee = crudService.createDomainObject("employee");
            employee.setString("Login", getPersonLogin(0));
            employee.setString("FirstName", getPersonLogin(0));
            employee.setString("LastName", getPersonLogin(0));
            employee.setString("EMail", getPersonLogin(0) + "@intertrast.ru");
            employee.setString("name", getPersonLogin(0));
            employee.setString("position", "big-boss");
            //Используем поле как счетчик сохранений
            employee.setString("certificate", "0");
            employee = crudService.save(employee);    
            
            int saveCount = Integer.valueOf(employee.getString("certificate"));
            assertTrue("save count", saveCount == 1);
 
        } finally {
            writeLog();
        }
    }    
    
    private String getPersonLogin(int num) {
        return "test_hierarchy_" + suffix + "_" + num;
    }    
}
