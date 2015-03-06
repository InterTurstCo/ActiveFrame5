package ru.intertrust.cm.core.business.impl.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.model.FatalException;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

/**
 * Класс имплементация SignatureDataService получения вложений для отправки
 * клиенту на ЭП Данная реализация получает все вложения за исключением
 * @author larin
 * 
 */
public class AllAttachmentSignatureDataService implements SignatureDataService {
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private CrudService crudService;

    private boolean isInhereFrom(String child, String parent) {
        boolean result = false;

        String checkType = child;
        while (checkType != null && !result) {
            result = checkType.equalsIgnoreCase(parent);
            checkType = configurationExplorer.getDomainObjectParentType(checkType);
        }

        return result;
    }

    protected InputStream getAttachmentStream(Id attachmentId) throws IOException {
        RemoteInputStream inputStream = attachmentService.loadAttachment(attachmentId);
        InputStream contentStream = RemoteInputStreamClient.wrap(inputStream);
        return contentStream;
    }

    @Override
    public List<Id> getBatchForSignature(CollectorSettings settings, Id rootId) {
        try {
            List<Id> result = new ArrayList<Id>();
            AllAttachmentSignatureDataSettings allAttachmentSignatureSettings = (AllAttachmentSignatureDataSettings) settings;
            //Получение всех вложений
            List<DomainObject> allAttachments = attachmentService.findAttachmentDomainObjectsFor(rootId);

            for (DomainObject domainObject : allAttachments) {
                boolean exclude = false;
                //Проверка на exclude
                if (allAttachmentSignatureSettings != null) {

                    if (allAttachmentSignatureSettings.getExludeAttachmentType() != null) {
                        for (String exludeType : allAttachmentSignatureSettings.getExludeAttachmentType()) {
                            if (isInhereFrom(domainObject.getTypeName(), exludeType)) {
                                exclude = true;
                                break;
                            }
                        }
                    }

                    if (allAttachmentSignatureSettings.getExludeAttachmentName() != null && !exclude) {
                        for (String exludeName : allAttachmentSignatureSettings.getExludeAttachmentName()) {
                            exclude = domainObject.getString("name").matches(exludeName);
                        }
                    }
                }
                if (!exclude) {
                    result.add(domainObject.getId());
                }
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error get signed data", ex);
        }
    }

    @Override
    public SignedDataItem getContentForSignature(CollectorSettings settings, Id id) {
        throw new FatalException("Domain object with id=" + id + " not has signable content.");
    }

}
