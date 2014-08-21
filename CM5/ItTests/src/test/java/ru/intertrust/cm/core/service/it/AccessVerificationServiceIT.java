package ru.intertrust.cm.core.service.it;

import java.io.IOException;
import java.util.Date;

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
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * 
 * @author atsvetkov
 *
 */
@RunWith(Arquillian.class)
public class AccessVerificationServiceIT extends IntegrationTestBase {

    @EJB
    private CrudService.Remote crudService;

    @EJB
    private AccessVerificationService.Remote accessVerificationService;
    
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

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
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
    }

    @Test
    public void testPermissionVerifications() {
        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);
        Id depId = savedDepartment.getId();

        accessVerificationService.isReadPermitted(depId);
        accessVerificationService.isWritePermitted(depId);
        accessVerificationService.isCreatePermitted(savedDepartment.getTypeName());
        accessVerificationService.isDeletePermitted(depId);
        accessVerificationService.isCreateChildPermitted(savedDepartment.getTypeName(), savedOrganization.getId());
        accessVerificationService.isExecuteActionPermitted("accessVerificationService", depId);

    }

    private DomainObject createOrganizationDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("Organization");
        organizationDomainObject.setString("Name", "Organization" + new Date());
        return organizationDomainObject;
    }

    private DomainObject createDepartmentDomainObject(DomainObject savedOrganizationObject) {
        DomainObject departmentDomainObject = crudService.createDomainObject("Department");
        departmentDomainObject.setString("Name", "department1");
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
    }
    
}
