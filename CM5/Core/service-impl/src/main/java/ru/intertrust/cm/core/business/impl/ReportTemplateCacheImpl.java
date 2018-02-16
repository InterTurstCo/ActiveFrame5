package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.service.api.ReportTemplateCache;

public class ReportTemplateCacheImpl implements ReportTemplateCache{

    @Value("${report.template.cache}")
    private String reportCachePath;

    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected AttachmentService attachmentService;

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Override
    public File getTemplateFolder(DomainObject reportTemplateDo) throws IOException {
        //Проверка есть директория для данного отчета в файловой системе, и если есть то проверка даты ее создания
        final String originalDatasource = currentDataSourceContext.get();
        currentDataSourceContext.setToMaster(); // read reports from MASTER DATASOURCE as slave might be outdated
        //Получение директории с шаблонами
        File templateFolder = new File(reportCachePath, reportTemplateDo.getString("name"));
        boolean dirCreated = false; 
        if (!templateFolder.exists()) {
            templateFolder.mkdirs();
            dirCreated = true;
        }

        //Сравнение даты изменения директории и даты создания доменного объекта шаблонов отчета 
        if (dirCreated || templateFolder.lastModified() < reportTemplateDo.getModifiedDate().getTime()) {
            //Шаблоны требуют перезачитывания
            //Удаляем все содержимое папки
            File[] files = templateFolder.listFiles();
            for (File file : files) {
                file.delete();
            }
            
            //Получение всех вложений
            List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateDo);
            for (DomainObject attachment : attachments) {
                byte[] content = getAttachmentContent(attachment);
                //Запись файла на диск
                writeToFile(content, new File(templateFolder, attachment.getString("Name")));
            }
            templateFolder.setLastModified(System.currentTimeMillis());
        }
        currentDataSourceContext.set(originalDatasource);
        return templateFolder;

    }

    private void writeToFile(byte[] content, File file) throws IOException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content);
        } finally {
            outStream.close();
        }
    }

    private byte[] getAttachmentContent(DomainObject attachment) {
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

    private List<DomainObject> getAttachments(String attachmentType, DomainObject attachmentOwner){
        String query = "select t.id from " + attachmentType + " t where t.report_template = " + ((RdbmsId)attachmentOwner.getId()).getId();
        
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        List<DomainObject> result = new ArrayList<DomainObject>();
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            result.add(domainObjectDao.find(identifiableObject.getId(), accessToken));
        }
        return result;
    }
}
