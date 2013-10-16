package ru.intertrust.cm.remoteclient.auditlog.test;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestAuditLog extends ClientBase {

    public static void main(String[] args) {
        try {
            TestAuditLog test = new TestAuditLog();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            
            CrudService.Remote crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);
            
            //Создание подразделения
            DomainObject department = crudService.createDomainObject("Department");
            department.setString("Name", "Department_" + System.currentTimeMillis());
            department = crudService.save(department);
            
            //Создание сотрудника
            DomainObject employee = crudService.createDomainObject("Employee");
            employee.setReference("Department", department.getId());
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("Phone", "+7" + System.currentTimeMillis());
            employee.setString("Login", "Login-" + System.currentTimeMillis());
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);

            //Изменение подразделения
            department.setString("Name", "Department_" + System.nanoTime());
            department = crudService.save(department);
            //Изменение подразделения
            department.setString("Name", "Department_" + System.nanoTime());
            department = crudService.save(department);

            //Изменение сотрудника
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("Phone", "+7" + System.currentTimeMillis());
            employee.setString("Login", "Login-" + System.currentTimeMillis());
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);
            //Изменение сотрудника
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("Phone", "+7" + System.currentTimeMillis());
            employee.setString("Login", "Login-" + System.currentTimeMillis());
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);
            
            //Удаление сотрудника
            crudService.delete(employee.getId());
            //Удаление подразделения
            crudService.delete(department.getId());
            
        } finally {
            writeLog();
        }
    }
}
