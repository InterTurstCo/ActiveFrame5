package ru.intertrust.cm.remoteclient.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.remoteclient.ClientBase;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

public class TestAttachments extends ClientBase {
    private CrudService.Remote crudService;

    private CollectionsService.Remote collectionService;

    private AttachmentService.Remote attachmentService;

    public static void main(String[] args) {
        try {
            TestAttachments test = new TestAttachments();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            collectionService = (CollectionsService.Remote) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);

            attachmentService = (AttachmentService.Remote) getService(
                    "AttachmentServiceImpl", AttachmentService.Remote.class);

            DomainObject person = createPerson();
            //Добавляем вложение 2 раза. Одно потом удалим второе оставим
            byte[] saveContent = readFile(new File("test.pdf"));

            DomainObject firstAttachment = setAttachment(person, new File("test.pdf"));
            DomainObject secondAttachment = setAttachment(person, new File("test.pdf"));
            System.out.println("Save OK");

            byte[] loadContent = getAttachmentContent(firstAttachment);
            boolean compareResult = compareContent(saveContent, loadContent);
            System.out.println("Load OK=" + compareResult);

            attachmentService.deleteAttachment(secondAttachment.getId());
            System.out.println("Delete OK");

        } finally {
            writeLog();
        }
    }

    private boolean compareContent(byte[] saveContent, byte[] loadContent) {
        if (saveContent.length != loadContent.length) {
            return false;
        }
        for (int i = 0; i < loadContent.length; i++) {
            if (saveContent[i] != loadContent[i]) {
                return false;
            }
        }
        return true;
    }

    private DomainObject createPerson() {
        DomainObject person = crudService.createDomainObject("Person");
        person.setString("Login", "person" + System.currentTimeMillis());
        person.setString("FirstName", "Person" + System.currentTimeMillis());
        person.setString("LastName", "Person" + System.currentTimeMillis());
        person.setString("EMail", "person" + System.currentTimeMillis() + "@intertrast.ru");
        person = crudService.save(person);
        return person;
    }

    private DomainObject setAttachment(DomainObject domainObject, File file) throws IOException {
        DomainObject attachment =
                attachmentService.createAttachmentDomainObjectFor(domainObject.getId(),
                        "report_template_attachment");
        attachment.setString("Name", file.getName());
        ByteArrayInputStream bis = new ByteArrayInputStream(readFile(file));
        SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(bis);

        RemoteInputStream remoteInputStream;
        remoteInputStream = simpleRemoteInputStream.export();
        DomainObject result = attachmentService.saveAttachment(remoteInputStream, attachment);
        return result;
    }

    protected byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0) {
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } catch (Exception ex) {
            throw new ReportServiceException("Error on get attachment body", ex);
        } finally {
            try {
                contentStream.close();
                inputStream.close(true);
            } catch (IOException ignoreEx) {
            }
        }
    }
}
