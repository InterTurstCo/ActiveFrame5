package ru.intertrust.cm.core.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

/**
 * Базовый класс имплементации сервисов подсистемы генерации отчетов
 *
 * @author larin
 */
public abstract class ReportServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceBase.class);

    private static File tempFolder;

    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected AttachmentService attachmentService;

    @Autowired
    protected StatusDao statusDao;

    @Autowired
    protected ConfigurationService configurationService;

    /**
     * Получение доменного объекта отчета по имени
     */
    protected DomainObject getReportTemplateObject(String name) {
        //TODO переделать на админ токен
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select t.id from report_template t where t.name = '" + name + '\'';
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
     */
    protected ReportMetadataConfig loadReportMetadata(byte[] body) {
        Serializer serializer = new Persister();
        ByteArrayInputStream stream = new ByteArrayInputStream(body);
        try {
            return serializer.read(ReportMetadataConfig.class, stream);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read report metadata", e);
        }
    }

    /**
     * Создание нового доменного объекта
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
     */
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception ex) {
                logger.error("Error read File", ex);
            }
        }
    }

    protected File getTempFolder() throws IOException {
        if (tempFolder == null) {
            File tmpFile = File.createTempFile("report_", "_service.tmp");
            tempFolder = tmpFile.getParentFile();
        }
        return tempFolder;
    }

    protected List<DomainObject> getAttachments(String attachmentType, Id attachmentOwnerId) {
        String query = "select t.id from " + attachmentType + " t where t.report_template = {0}";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        return collectionsDao.findCollectionByQuery(query, Collections.singletonList(new ReferenceValue(attachmentOwnerId)), 0, 0, accessToken)
                .stream()
                .map(io -> domainObjectDao.find(io.getId(), accessToken))
                .collect(Collectors.toList());
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }
}
