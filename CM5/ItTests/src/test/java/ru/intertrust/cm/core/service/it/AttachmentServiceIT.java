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

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.DomainObjectConfig;
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
        return createDeployment(new Class[] {AttachmentServiceIT.class, ApplicationContextProvider.class}, new String[] {"test-data/import-department.csv",
                "test-data/import-organization.csv",
                "test-data/import-employee.csv", "beans.xml"});
    }

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
        try {
            importTestData("test-data/import-organization.csv");
            importTestData("test-data/import-employee.csv");
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
    public void testSaveAttachment() throws FileNotFoundException {
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("/beans.xml");

        DomainObject countryObject = createCountryDomainObject();
        DomainObject savedCountryObject = crudService.save(countryObject);

        DomainObjectTypeConfig countryConfig = configurationService.getConfig(DomainObjectTypeConfig.class, "country");
        String attachmentType = countryConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0).getName();

        RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(inputStream);
        DomainObject attachmentDomainObject = attachmentService.
                createAttachmentDomainObjectFor(savedCountryObject.getId(), attachmentType);

        attachmentDomainObject.setValue(AttachmentService.NAME, new StringValue("Attachment"));
        attachmentDomainObject.setValue(AttachmentService.DESCRIPTION, new StringValue("Attachment Description"));
        // TODO get file path

        String mimeType = null;
        try {
            mimeType = Files.probeContentType(Paths.get(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mimeType = mimeType == null ? "undefined" : mimeType;
        attachmentDomainObject.setValue(AttachmentService.MIME_TYPE, new StringValue(mimeType));
        int contentLength = 10;
        attachmentDomainObject.setValue(AttachmentService.CONTENT_LENGTH, new LongValue(contentLength));

        attachmentDomainObject.setReference("country", savedCountryObject);
        DomainObject attachment = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
        
        List<DomainObject> attachemnts = attachmentService.findAttachmentDomainObjectsFor(savedCountryObject.getId(), attachmentType);
        assertTrue(attachemnts.size() > 0);
    }

    public void testLoadAttachment() throws FileNotFoundException {
    
    }
    
    public void testDeleteAttachment() throws FileNotFoundException {
        
    }

    public void testFindAttachmentDomainObjectsFor() throws FileNotFoundException {
        
    }


         private static GenericDomainObject createCountryDomainObject() {
             GenericDomainObject organizationDomainObject = new GenericDomainObject();
             organizationDomainObject.setCreatedDate(new Date());
             organizationDomainObject.setModifiedDate(new Date());
             organizationDomainObject.setTypeName("Country");
             organizationDomainObject.setString("Name", "Country" + new Date());
             return organizationDomainObject;
         }

    
}
