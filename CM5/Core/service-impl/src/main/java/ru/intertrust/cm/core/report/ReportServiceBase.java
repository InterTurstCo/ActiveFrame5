package ru.intertrust.cm.core.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Базовый класс имплементации сервисов подсистемы генерации отчетов
 * @author larin
 * 
 */
public abstract class ReportServiceBase {
    private static File tempFolder;
    
    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected AttachmentService attachmentService;

    /**
     * Получение доменного объекта отчета по имени
     * @param name
     * @return
     */
    protected DomainObject getReportTemplateObject(String name) {
        //TODO переделать на админ токен
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select t.id from report_template t where t.name = '" + name + "'";
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 100, accessToken);
        DomainObject result = null;
        if (collection.size() > 0) {
            IdentifiableObject row = collection.get(0);
            result = domainObjectDao.find(row.getId(), accessToken);
        }
        return result;
    }

    /**
     * Получение метаинформации в виде доменного объекта
     * @param body
     * @return
     * @throws Exception
     */
    protected ReportMetadataConfig loadReportMetadata(byte[] body) throws Exception {
        Serializer serializer = new Persister();
        ByteArrayInputStream stream = new ByteArrayInputStream(body);
        ReportMetadataConfig config = serializer.read(ReportMetadataConfig.class, stream);
        return config;
    }
/*
    protected byte[] getAttachmentContent(DomainObject attachment) {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachment.getId());
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();
            
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0){
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
*/
    /**
     * Создание нового доменного объекта
     * 
     * @param type
     * @return
     */
    protected DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }
/*
    /**
     * Запись массива байт в файл
     * @param content
     * @param file
     * @throws IOException
     * /
    protected void writeToFile(byte[] content, File file) throws IOException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content);
        } finally {
            outStream.close();
        }
    }
*/    
    protected File getTempFolder() throws IOException {
        if (tempFolder == null) {
            File tmpFile = File.createTempFile("report_", "_service.tmp");
            tempFolder = tmpFile.getParentFile();
        }
        return tempFolder;
    }

    protected List<DomainObject> getAttachments(String attachmentType, Id attachmentOwnerId){
        String query = "select t.id from " + attachmentType + " t where t.report_template = {0}";
        
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        List<DomainObject> result = new ArrayList<DomainObject>();
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(attachmentOwnerId));
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
        }
        return result;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

}
