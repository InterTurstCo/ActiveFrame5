package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Интеграционный тест работы с вложениями.
 * @author atsvetkov
 *
 */
@RunWith(Arquillian.class)
public class AttachmentServiceIT extends IntegrationTestBase {

    @EJB
    private AttachmentService.Remote attachmentService;
    
    @EJB
    ConfigurationService.Remote configurationService;    
    
    @EJB    
    private CrudService.Remote crudService;
    
    @Deployment
    public static Archive<EnterpriseArchive> createDeployment() {
        return createDeployment(new Class[] {AttachmentServiceIT.class, ApplicationContextProvider.class}, new String[] {"beans.xml"});
    }

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
        try {
        } finally {
            lc.logout();
        }
        initializeSpringBeans();
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
    }

    @Test
    public void testSaveFindAttachment() throws FileNotFoundException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("/beans.xml");
        RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(inputStream);

        DomainObject countryObject = createCountryDomainObject();
        DomainObject savedCountryObject = crudService.save(countryObject);

        DomainObjectTypeConfig countryConfig = configurationService.getConfig(DomainObjectTypeConfig.class, "country");
        String attachmentType = countryConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0).getName();

        DomainObject attachmentDomainObject = createAttachmentDomainObject(savedCountryObject.getId(), attachmentType);

        DomainObject attachment = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
        assertNotNull(attachment);

        RemoteInputStream loadedData = attachmentService.loadAttachment(attachment.getId());
        assertNotNull(loadedData);

        List<DomainObject> attachments =
                attachmentService.findAttachmentDomainObjectsFor(savedCountryObject.getId(), attachmentType);
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

        attachments = attachmentService.findAttachmentDomainObjectsFor(savedCountryObject.getId());
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));

        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

        attachmentService.deleteAttachment(attachment.getId());
        attachments =
                attachmentService.findAttachmentDomainObjectsFor(savedCountryObject.getId(), attachmentType);
        assertTrue(attachments.size() == 0);

    }

    /**
     * Employee -> Person (which declares Attachments)
     * @throws FileNotFoundException
     */
    @Test
    public void testSaveFindAttachmentForHierarchicalOwningObject() throws FileNotFoundException {

        DomainObject organization = createOrganizationDomainObject();
        DomainObject savedOrganization = crudService.save(organization);
        DomainObject department = createDepartmentDomainObject(savedOrganization);
        DomainObject savedDepartment = crudService.save(department);

        DomainObject childDoc = createEmployeeDomainObject(savedDepartment);
        childDoc = crudService.save(childDoc);

        Id childDocId = childDoc.getId();
        DomainObject attachment = createAttachmentDomainObject(childDocId, "Person_Attachment");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("/beans.xml");
        RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(inputStream);

        attachment = attachmentService.saveAttachment(remoteFileData, attachment);

        List<DomainObject> attachments =
                attachmentService.findAttachmentDomainObjectsFor(childDocId, "Person_Attachment");
        System.out.println(Integer.toString(attachments.size()) + " attachment(s) found");
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));
    }

    private DomainObject createAttachmentDomainObject(Id childDocId, String attachmentType) {
        DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(childDocId, attachmentType);
        attachment.setValue(AttachmentService.NAME, new StringValue("Attachment"));
        attachment.setValue(AttachmentService.DESCRIPTION, new StringValue("Attachment Description"));
        String mimeType = "text/xml";
        attachment.setValue(AttachmentService.MIME_TYPE, new StringValue(mimeType));
        int contentLength = 10;
        attachment.setValue(AttachmentService.CONTENT_LENGTH, new LongValue(contentLength));       
        return attachment;
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

    private DomainObject createEmployeeDomainObject(DomainObject department) {
        DomainObject personDomainObject = crudService.createDomainObject("Employee");
        personDomainObject.setString("Name", "Name of Emplyee");
        personDomainObject.setString("Position", "Position1");        
        personDomainObject.setReference("Department", department.getId());

        return personDomainObject;
    }

    private DomainObject createCountryDomainObject() {
        DomainObject domainObject = crudService.createDomainObject("Country");
        domainObject.setString("Name", "Country" + new Date());
        return domainObject;
    }
    
}
