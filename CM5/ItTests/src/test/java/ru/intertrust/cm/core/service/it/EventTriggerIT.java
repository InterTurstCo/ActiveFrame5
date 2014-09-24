package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.EventTrigger;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.FieldModificationImpl;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * 
 * @author atsvetkov
 *
 */
@RunWith(Arquillian.class)
public class EventTriggerIT extends IntegrationTestBase {

    private EventTrigger eventTrigger;
    
    private StatusDao statusDao;
    
    @EJB
    private CrudService.Remote crudService;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
        initializeSpringBeans();

    }

    @After
    public void tearDown() throws LoginException {
        LoginContext lc = login("admin", "admin");
        lc.logout();
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        eventTrigger = applicationContext.getBean(EventTrigger.class);
        statusDao = applicationContext.getBean(StatusDao.class);
    }
    
    @Test
    public void testReadTriggerConfiguration() {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        
        DomainObject domainObject = createDepartmentTestDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();
        changedFileds.add(new FieldModificationImpl("Name", new StringValue("Department1"), new StringValue("Department2")));        
        boolean isTriggered = eventTrigger.isTriggered("TriggerReadConfig", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);
        
        changedFileds = new ArrayList<FieldModification>();
        changedFileds.add(new FieldModificationImpl("Boss", new StringValue("Boss1"), new StringValue("Boss2")));        
        isTriggered = eventTrigger.isTriggered("TriggerReadConfig", "CHANGE", domainObject, changedFileds);
        assertTrue(!isTriggered);
        
        changedFileds = new ArrayList<FieldModification>();
        changedFileds.add(new FieldModificationImpl("Name", new StringValue("Department1"), new StringValue("Department2")));        
        isTriggered = eventTrigger.isTriggered("TriggerReadConfig", "CREATE", domainObject, changedFileds);
        assertTrue(!isTriggered);        
        
    }

    @Test
    public void testTriggerChangeStatus() {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        
        DomainObject domainObject = createDepartmentTestDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);
        List<FieldModification> changedFileds = new ArrayList<FieldModification>();
        changedFileds.add(new FieldModificationImpl("Name", new StringValue("Department1"), new StringValue("Department2")));        
        boolean isTriggered = eventTrigger.isTriggered("TriggerChangeStatus", "CHANGE_STATUS", domainObject, changedFileds);
        assertTrue(isTriggered);
    }

    @Test
    public void testExecuteScript() {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject domainObject = createDepartmentTestDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("TriggerScript", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);
    }

    @Test
    public void testExecuteComplexScript() {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject departmentObject = createDepartmentTestDomainObject(organizationDomainObject);
        departmentObject = crudService.save(departmentObject);

        DomainObject employee = createEmployeeTestDomainObject(departmentObject);        
        DomainObject savedEmployee = crudService.save(employee);

        
        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("TriggerTestScript", "CHANGE", departmentObject, changedFileds);
        assertTrue(isTriggered);
        
        organizationDomainObject.setValue("Name", new StringValue("Organization2"));
        organizationDomainObject = crudService.save(organizationDomainObject);
        departmentObject = createDepartmentTestDomainObject(organizationDomainObject);
        departmentObject.setValue("Name", new StringValue("Department2"));
        departmentObject = crudService.save(departmentObject);

        changedFileds = new ArrayList<FieldModification>();        
        isTriggered = eventTrigger.isTriggered("TriggerTestScript", "CHANGE", departmentObject, changedFileds);
        assertTrue(!isTriggered);
    }
    
    @Test
    public void testExecuteJavaClass() {
        DomainObject organizationDomainObject = createOrganizationTestDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject domainObject = createDepartmentTestDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("Trigger2", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);

        isTriggered = eventTrigger.isTriggered("Trigger2", "DELETE", domainObject, changedFileds);
        assertTrue(!isTriggered);

    }

    private DomainObject createEmployeeTestDomainObject(DomainObject departmentObject) {
        DomainObject employeeDomainObject = crudService.createDomainObject("employee_test");
        
        employeeDomainObject.setString("Name", "Name " + System.currentTimeMillis());
        employeeDomainObject.setString("Position", "Position " + System.currentTimeMillis());
        employeeDomainObject.setString("Phone", "" + System.currentTimeMillis()); 
        employeeDomainObject.setString("Login", "Login" + System.currentTimeMillis()); 
        employeeDomainObject.setString("EMail", "Email" + System.currentTimeMillis()); 
        
        employeeDomainObject.setReference("Department", departmentObject.getId());
        
        return employeeDomainObject;
    }

    private DomainObject createDepartmentTestDomainObject(DomainObject organizationDomainObject) {
        
        DomainObject departmentDomainObject = crudService.createDomainObject("department_test");
        departmentDomainObject.setString("Name", "Departmment");
        departmentDomainObject.setLong("Number1", new Long(1));
        departmentDomainObject.setLong("Number2", new Long(2));
        
        departmentDomainObject.setTimestamp("Date1", new Date());
        departmentDomainObject.setTimestamp("Date2", new Date());

        departmentDomainObject.setReference("Organization", organizationDomainObject.getId());
        return departmentDomainObject;
    }

    private DomainObject createOrganizationTestDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("organization_test");
        organizationDomainObject.setString("Name", "Organization");
        return organizationDomainObject;
    }

}

