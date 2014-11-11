package ru.intertrust.cm.core.service.it;

import java.io.*;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import static org.junit.Assert.*;

/**
 * Интеграционный тест работы с вложениями.
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class AttachmentServiceIT extends IntegrationTestBase {

    private static final int BUF_SIZE = 0x1000;

    @EJB
    private AttachmentService.Remote attachmentService;

    @EJB
    ConfigurationService.Remote configurationService;

    @EJB
    private CrudService.Remote crudService;

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

        attachments = attachmentService.findAttachmentDomainObjectsFor(childDocId);
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

    }

    @Test
    public void testCopyAttachment() throws IOException {

        InputStream loadedData = null;
        InputStream testData = null;

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("/beans.xml");
            RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(inputStream);

            DomainObject countryObject = createCountryDomainObject();
            countryObject = crudService.save(countryObject);

            DomainObjectTypeConfig countryConfig = configurationService.getConfig(DomainObjectTypeConfig.class, "country");
            String attachmentType = countryConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0).getName();

            DomainObject attachmentDomainObject = createAttachmentDomainObject(countryObject.getId(), attachmentType);
            attachmentDomainObject = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
            RemoteInputStream loadedRemoteData = attachmentService.loadAttachment(attachmentDomainObject.getId());
            loadedData = RemoteInputStreamClient.wrap(loadedRemoteData);
            ByteArrayOutputStream loadedBytes = new ByteArrayOutputStream();
            copy(loadedData, loadedBytes);

            DomainObject destinationObject = createCountryDomainObject();
            destinationObject = crudService.save(destinationObject);

            DomainObject copiedAttachmentDomainObject =
                    attachmentService.copyAttachment(attachmentDomainObject.getId(), destinationObject.getId(), attachmentType);

            List<DomainObject> testAttachmentDomainObjects = attachmentService.findAttachmentDomainObjectsFor(destinationObject.getId());
            assertNotNull(testAttachmentDomainObjects);
            assertTrue(testAttachmentDomainObjects.size() == 1);

            DomainObject testAttachmentDomainObject = testAttachmentDomainObjects.get(0);
            assertEquals(copiedAttachmentDomainObject.getId(), testAttachmentDomainObject.getId());
            assertEquals(copiedAttachmentDomainObject.getLong(BaseAttachmentService.CONTENT_LENGTH),
                    testAttachmentDomainObject.getLong(BaseAttachmentService.CONTENT_LENGTH));
            assertEquals(copiedAttachmentDomainObject.getString(BaseAttachmentService.DESCRIPTION),
                    testAttachmentDomainObject.getString(BaseAttachmentService.DESCRIPTION));
            assertEquals(copiedAttachmentDomainObject.getString(BaseAttachmentService.MIME_TYPE),
                    testAttachmentDomainObject.getString(BaseAttachmentService.MIME_TYPE));
            assertEquals(copiedAttachmentDomainObject.getString(BaseAttachmentService.NAME),
                    testAttachmentDomainObject.getString(BaseAttachmentService.NAME));

            RemoteInputStream testRemoteData = attachmentService.loadAttachment(testAttachmentDomainObject.getId());
            testData = RemoteInputStreamClient.wrap(testRemoteData);
            ByteArrayOutputStream testBytes = new ByteArrayOutputStream();
            copy(testData, testBytes);
            assertArrayEquals(loadedBytes.toByteArray(), testBytes.toByteArray());
        } finally {
            if (loadedData != null) {
                loadedData.close();
            }

            if (testData != null) {
                testData.close();
            }
        }

    }

    private DomainObject createAttachmentDomainObject(Id childDocId, String attachmentType) {
        DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(childDocId, attachmentType);
        attachment.setValue(BaseAttachmentService.NAME, new StringValue("Attachment"));
        attachment.setValue(BaseAttachmentService.DESCRIPTION, new StringValue("Attachment Description"));
        String mimeType = "text/xml";
        attachment.setValue(BaseAttachmentService.MIME_TYPE, new StringValue(mimeType));
        int contentLength = 10;
        attachment.setValue(BaseAttachmentService.CONTENT_LENGTH, new LongValue(contentLength));
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
        domainObject.setString("Name", "Country" + System.currentTimeMillis());
        return domainObject;
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
        }
    }

}
