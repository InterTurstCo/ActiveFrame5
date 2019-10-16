package ru.intertrust.cm.remoteclient.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
                    "RemoteAttachmentServiceImpl", AttachmentService.Remote.class);

            DomainObject person = createPerson();
            //Добавляем вложение 2 раза. Одно потом удалим второе оставим
            String file = "test.pdf";
            byte[] saveContent = readFile(new File(file));

            DomainObject firstAttachment = setAttachment(person, new File(file));
            DomainObject secondAttachment = setAttachment(person, new File(file));
            System.out.println("Save OK " + saveContent.length);

            List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(person.getId());
            assertTrue("findAttachmentDomainObjectsFor", attachments.size() == 2);

            byte[] loadContent = getAttachmentContent(firstAttachment);
            boolean compareResult = compareContent(saveContent, loadContent);
            assertTrue("Contents equals", compareResult);
            System.out.println("Load " + compareResult + " " + loadContent.length);

            attachmentService.deleteAttachment(secondAttachment.getId());
            System.out.println("Delete OK");

            attachments = attachmentService.findAttachmentDomainObjectsFor(person.getId());
            assertTrue("findAttachmentDomainObjectsFor", attachments.size() == 1);


            //Проверка распознования типа файла (mimetype)
            DomainObject testTypeDo = setAttachment(person, new File(file));
            assertTrue("PDF", testTypeDo.getString("mimetype").equalsIgnoreCase("application/pdf"));
            
            testTypeDo = setAttachment(person, new File("import-employee.csv"));
            assertTrue("CSV", testTypeDo.getString("mimetype").equalsIgnoreCase("text/csv"));

            testTypeDo = setAttachment(person, new File("test_1251.txt"));
            assertTrue("TXT", testTypeDo.getString("mimetype").equalsIgnoreCase("text/plain"));

            testTypeDo = setAttachment(person, new File("test_utf_8.txt"));
            assertTrue("TXT", testTypeDo.getString("mimetype").equalsIgnoreCase("text/plain"));

            testTypeDo = setAttachment(person, new File("test.bmp"));
            assertTrue("BMP", testTypeDo.getString("mimetype").equalsIgnoreCase("image/x-ms-bmp"));

            testTypeDo = setAttachment(person, new File("test.doc"));
            assertTrue("DOC", testTypeDo.getString("mimetype").equalsIgnoreCase("application/msword"));
            
            testTypeDo = setAttachment(person, new File("test.docx"));
            assertTrue("DOCX", testTypeDo.getString("mimetype").equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            
            testTypeDo = setAttachment(person, new File("test.jpg"));
            assertTrue("JPG", testTypeDo.getString("mimetype").equalsIgnoreCase("image/jpeg"));
            
            testTypeDo = setAttachment(person, new File("test.odt"));
            assertTrue("ODT", testTypeDo.getString("mimetype").equalsIgnoreCase("application/vnd.oasis.opendocument.text"));

            testTypeDo = setAttachment(person, new File("test.rtf"));
            assertTrue("RTF", testTypeDo.getString("mimetype").equalsIgnoreCase("application/rtf"));

            testTypeDo = setAttachment(person, new File("test"));
            assertTrue("PDF without extension", testTypeDo.getString("mimetype").equalsIgnoreCase("application/octet-stream"));

            testTypeDo = setAttachment(person, new File("test.xls"));
            assertTrue("XLS", testTypeDo.getString("mimetype").equalsIgnoreCase("application/vnd.ms-excel"));

            testTypeDo = setAttachment(person, new File("test.xlsx"));
            assertTrue("XLSX", testTypeDo.getString("mimetype").equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            attachments = attachmentService.findAttachmentDomainObjectsFor(person.getId());
            assertTrue("findAttachmentDomainObjectsFor", attachments.size() == 14);


            log("Tset OK");
            testTime();

        } finally {
            writeLog();
        }
    }

    private byte[] createLageContent(int size) throws Exception{
        String file = "test.pdf";
        byte[] fileContent = readFile(new File(file));
        ByteArrayOutputStream saveStream = new ByteArrayOutputStream();
        while(saveStream.size() < size * 1024 * 1024){
            saveStream.write(fileContent);
        }
        return saveStream.toByteArray();
    }
    
    private void testTime() throws Exception {
        DomainObject person = createPerson();

        byte[] fileContent = createLageContent(15);

        ByteArrayOutputStream saveStream = new ByteArrayOutputStream();
        saveStream.write(fileContent, 0, 100 * 1024);
        byte[] saveContent = saveStream.toByteArray();
        int iteration = 1;
        int size = 100 * 1024;
        while (fileContent.length > size) {
            saveStream.reset();
            saveStream.write(fileContent, 0, size);
            saveContent = saveStream.toByteArray();
            
            long start = System.currentTimeMillis();
            DomainObject attachment = setAttachment(person, "test.pdf", saveContent);
            long save = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            byte[] loadContent = getAttachmentContent(attachment);
            long load = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            attachmentService.deleteAttachment(attachment.getId());
            long delete = System.currentTimeMillis() - start;
            
            System.out.println("Size=" + saveContent.length + "\tSave=" + save + "\tLoad=" + load + "\tDelete=" + delete);
            
            boolean compareRes = compareContent(saveContent, loadContent);
            if (!compareRes){
                assertTrue("Compare content", compareRes);
            }
            
            
            //Увеличиваем размер контента логарифмически
            int pow = (int) (Math.pow(2, iteration));
            size = 100 * 1024 * pow;
            iteration++;
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
        return setAttachment(domainObject, file.getName(), readFile(file));
    }

    private DomainObject setAttachment(DomainObject domainObject, String name, byte[] content) throws IOException {
        DomainObject attachment =
                attachmentService.createAttachmentDomainObjectFor(domainObject.getId(),
                        "Person_Attachment");
        attachment.setString("Name", name);
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
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
                if (contentStream != null){
                    contentStream.close();
                }
                if (inputStream != null) {
                    inputStream.close(true);
                }
            } catch (IOException ignoreEx) {
            }
        }
    }
}
