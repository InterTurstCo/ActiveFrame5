package ru.intertrust.cm.remoteclient.auditlog.test;

import java.util.List;

import ru.intertrust.cm.core.business.api.AuditService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.VersionComparisonResult;
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

            AuditService.Remote auditService = (AuditService.Remote) getService(
                    "AuditService", AuditService.Remote.class);

            // Создание подразделения
            DomainObject department = crudService.createDomainObject("Department");
            department.setString("Name", "Department_" + System.currentTimeMillis());
            department = crudService.save(department);
            //Получение всех версий
            List<DomainObjectVersion> versions = auditService.findAllVersions(department.getId());
            assertTrue("Get department version after create", versions.size() == 1);
            //Получение одной версии
            DomainObjectVersion version = auditService.findVersion(versions.get(0).getId());
            assertTrue("Get department version", version.getString("Name").equals(department.getString("Name")));

            // Создание сотрудника
            DomainObject employee = crudService.createDomainObject("Employee");
            employee.setReference("Department", department.getId());
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("Phone", "+7" + System.currentTimeMillis());
            employee.setString("Login", "Login-" + System.currentTimeMillis());
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);
            //Получение всех версий сотрудника
            versions = auditService.findAllVersions(employee.getId());
            assertTrue("Get Employee version after create", versions.size() == 1);
            //Получение одной версии сотрудника
            version = auditService.findVersion(versions.get(0).getId());
            assertTrue("Get Employee version", version.getString("Login").equals(employee.getString("Login")));
            
            // Изменение подразделения
            department.setString("Name", "Department_" + System.nanoTime());
            department = crudService.save(department);
            //Получение версий после изменения
            versions = auditService.findAllVersions(department.getId());
            assertTrue("Get Department version after update", versions.size() == 2);            
            
            // Изменение подразделения
            department.setString("Name", "Department_" + System.nanoTime());
            department = crudService.save(department);
            //Получение версий после изменения
            versions = auditService.findAllVersions(department.getId());
            assertTrue("Get Department version after update", versions.size() == 3);            
            //Сравнение версий
            VersionComparisonResult comparisonResult =  auditService.compare(versions.get(0).getId(), versions.get(2).getId());
            assertTrue("Compare department versions", comparisonResult.getModifiedFields().size() == 1);
            comparisonResult =  auditService.compare(versions.get(1).getId());
            assertTrue("Compare department versions", comparisonResult.getModifiedFields().size() == 1);
            
            // Изменение сотрудника
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("Phone", "+7" + System.currentTimeMillis());
            employee.setString("Login", "Login-" + System.currentTimeMillis());
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);
            //Получение всех версий сотрудника
            versions = auditService.findAllVersions(employee.getId());
            assertTrue("Get Employee version after update", versions.size() == 2);
            
            // Изменение сотрудника
            employee.setString("Name", "Employee_" + System.currentTimeMillis());
            employee.setString("Position", "Сотрудник");
            employee.setString("FirstName", "FirstName_" + System.currentTimeMillis());
            employee = crudService.save(employee);
            //Получение всех версий сотрудника
            versions = auditService.findAllVersions(employee.getId());
            assertTrue("Get Employee version after update", versions.size() == 3);            
            //Сравнение версий
            comparisonResult =  auditService.compare(versions.get(0).getId(), versions.get(1).getId());
            assertTrue("Compare employee versions", comparisonResult.getModifiedFields().size() == 4);
            comparisonResult =  auditService.compare(versions.get(1).getId());
            assertTrue("Compare employee versions", comparisonResult.getModifiedFields().size() == 2);

            // Удаление сотрудника
            crudService.delete(employee.getId());
            //Получение всех версий сотрудника
            versions = auditService.findAllVersions(employee.getId());
            assertTrue("Get Employee version after delete", versions.size() == 4);
            
            // Удаление подразделения
            crudService.delete(department.getId());
            //Получение версий после удаления
            versions = auditService.findAllVersions(department.getId());
            assertTrue("Get Department version after delete", versions.size() == 4);    
            
            //Удаление лога сотрудника
            auditService.clean(employee.getId());
            //Получение версий после очистки лога
            versions = auditService.findAllVersions(employee.getId());
            assertTrue("Get Employee version after clean", versions.size() == 0);
            
            //Удаление лога подразделения
            auditService.clean(department.getId());
            //Получение версий после очистки лога
            versions = auditService.findAllVersions(department.getId());
            assertTrue("Get Department version after clrean", versions.size() == 0);    

        } finally {
            writeLog();
        }
    }

}
