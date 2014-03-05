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
        DomainObject organizationDomainObject = createOrganizationDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        
        DomainObject domainObject = createDepartmentDomainObject(organizationDomainObject);
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
        DomainObject organizationDomainObject = createOrganizationDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        
        DomainObject domainObject = createDepartmentDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);
        List<FieldModification> changedFileds = new ArrayList<FieldModification>();
        changedFileds.add(new FieldModificationImpl("Name", new StringValue("Department1"), new StringValue("Department2")));        
        boolean isTriggered = eventTrigger.isTriggered("TriggerChangeStatus", "CHANGE_STATUS", domainObject, changedFileds);
        assertTrue(isTriggered);
    }

    @Test
    public void testExecuteScript() {
        DomainObject organizationDomainObject = createOrganizationDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject domainObject = createDepartmentDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("TriggerScript", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);
    }

    @Test
    public void testExecuteComplexScript() {
        DomainObject organizationDomainObject = createOrganizationDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject domainObject = createDepartmentDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("TriggerTestScript", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);
        
        organizationDomainObject.setValue("Name", new StringValue("Organization2"));
        organizationDomainObject = crudService.save(organizationDomainObject);
        domainObject = createDepartmentDomainObject(organizationDomainObject);
        domainObject.setValue("Name", new StringValue("Department2"));
        domainObject = crudService.save(domainObject);

        changedFileds = new ArrayList<FieldModification>();        
        isTriggered = eventTrigger.isTriggered("TriggerTestScript", "CHANGE", domainObject, changedFileds);
        assertTrue(!isTriggered);
    }
    
    @Test
    public void testExecuteJavaClass() {
        DomainObject organizationDomainObject = createOrganizationDomainObject();
        organizationDomainObject = crudService.save(organizationDomainObject);
        DomainObject domainObject = createDepartmentDomainObject(organizationDomainObject);
        domainObject = crudService.save(domainObject);

        List<FieldModification> changedFileds = new ArrayList<FieldModification>();        
        boolean isTriggered = eventTrigger.isTriggered("Trigger2", "CHANGE", domainObject, changedFileds);
        assertTrue(isTriggered);

        isTriggered = eventTrigger.isTriggered("Trigger2", "DELETE", domainObject, changedFileds);
        assertTrue(!isTriggered);

    }

    private DomainObject createDepartmentDomainObject(DomainObject organizationDomainObject) {
        
        DomainObject departmentDomainObject = crudService.createDomainObject("department_test");
        departmentDomainObject.setString("Name", "Departmment");
        departmentDomainObject.setReference("Organization", organizationDomainObject.getId());
        return departmentDomainObject;
    }

    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("organization_test");
        organizationDomainObject.setString("Name", "Organization");
        return organizationDomainObject;
    }

}

