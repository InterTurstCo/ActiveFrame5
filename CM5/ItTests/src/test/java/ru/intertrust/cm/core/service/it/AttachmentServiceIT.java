package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.healthmarketscience.rmiio.*;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

/**
 * Интеграционный тест работы с вложениями.
 * @author atsvetkov
 */
@RunWith(Arquillian.class)
public class AttachmentServiceIT extends IntegrationTestBase {

    private static final int BUF_SIZE = 0x1000;

    @EJB
    private AttachmentService.Remote attachmentServiceRemote;

    @EJB
    private AttachmentService attachmentServiceLocal;

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

        DomainObject attachment = attachmentServiceRemote.saveAttachment(remoteFileData, attachmentDomainObject);
        assertNotNull(attachment);

        RemoteInputStream loadedData = attachmentServiceRemote.loadAttachment(attachment.getId());
        assertNotNull(loadedData);

        List<DomainObject> attachments =
                attachmentServiceRemote.findAttachmentDomainObjectsFor(savedCountryObject.getId(), attachmentType);
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

        attachments = attachmentServiceRemote.findAttachmentDomainObjectsFor(savedCountryObject.getId());
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));

        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

        attachmentServiceRemote.deleteAttachment(attachment.getId());
        attachments =
                attachmentServiceRemote.findAttachmentDomainObjectsFor(savedCountryObject.getId(), attachmentType);
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

        attachment = attachmentServiceRemote.saveAttachment(remoteFileData, attachment);

        List<DomainObject> attachments =
                attachmentServiceRemote.findAttachmentDomainObjectsFor(childDocId, "Person_Attachment");
        System.out.println(Integer.toString(attachments.size()) + " attachment(s) found");
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

        attachments = attachmentServiceRemote.findAttachmentDomainObjectsFor(childDocId);
        assertTrue(attachments.size() > 0);
        assertNotNull(attachments.get(0));
        assertTrue(attachment.getId().equals(attachments.get(0).getId()));

    }

    @Test
    public void testCopyAttachmentLocal() throws IOException {

        InputStream loadedData = null;
        InputStream testData = null;

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("/beans.xml");
            DirectRemoteInputStream remoteFileData = new DirectRemoteInputStream(inputStream, false);

            DomainObject countryObject = createCountryDomainObject();
            countryObject = crudService.save(countryObject);

            DomainObjectTypeConfig countryConfig = configurationService.getConfig(DomainObjectTypeConfig.class, "country");
            String attachmentType = countryConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0).getName();

            DomainObject attachmentDomainObject = createAttachmentDomainObject(countryObject.getId(), attachmentType);
            attachmentDomainObject = attachmentServiceLocal.saveAttachment(remoteFileData, attachmentDomainObject);
            RemoteInputStream loadedRemoteData = attachmentServiceLocal.loadAttachment(attachmentDomainObject.getId());
            loadedData = RemoteInputStreamClient.wrap(loadedRemoteData);
            ByteArrayOutputStream loadedBytes = new ByteArrayOutputStream();
            copy(loadedData, loadedBytes);

            DomainObject destinationObject = createCountryDomainObject();
            destinationObject = crudService.save(destinationObject);

            DomainObject copiedAttachmentDomainObject =
                    attachmentServiceLocal.copyAttachment(attachmentDomainObject.getId(), destinationObject.getId(), attachmentType);

            List<DomainObject> testAttachmentDomainObjects = attachmentServiceLocal.findAttachmentDomainObjectsFor(destinationObject.getId());
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

            RemoteInputStream testRemoteData = attachmentServiceRemote.loadAttachment(testAttachmentDomainObject.getId());
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

    @Test
    public void testCopyAttachmentRemote() throws IOException {

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
            attachmentDomainObject = attachmentServiceRemote.saveAttachment(remoteFileData, attachmentDomainObject);
            RemoteInputStream loadedRemoteData = attachmentServiceRemote.loadAttachment(attachmentDomainObject.getId());
            loadedData = RemoteInputStreamClient.wrap(loadedRemoteData);
            ByteArrayOutputStream loadedBytes = new ByteArrayOutputStream();
            copy(loadedData, loadedBytes);

            DomainObject destinationObject = createCountryDomainObject();
            destinationObject = crudService.save(destinationObject);

            DomainObject copiedAttachmentDomainObject =
                    attachmentServiceRemote.copyAttachment(attachmentDomainObject.getId(), destinationObject.getId(), attachmentType);

            List<DomainObject> testAttachmentDomainObjects = attachmentServiceRemote.findAttachmentDomainObjectsFor(destinationObject.getId());
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

            RemoteInputStream testRemoteData = attachmentServiceRemote.loadAttachment(testAttachmentDomainObject.getId());
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
        DomainObject attachment = attachmentServiceRemote.createAttachmentDomainObjectFor(childDocId, attachmentType);
        attachment.setValue(BaseAttachmentService.NAME, new StringValue(GenericDomainObject.ATTACHMENT_TEMPLATE));
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
