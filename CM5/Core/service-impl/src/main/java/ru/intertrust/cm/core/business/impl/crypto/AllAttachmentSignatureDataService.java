package ru.intertrust.cm.core.business.impl.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.SignedData;
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

    @Override
    public SignedData getSignedData(CollectorSettings settings, Id rootId) {
        try {
            SignedData result = new SignedData();
            result.setRootId(rootId);
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
                    result.getSignedDataItems().add(
                            new SignedDataItem(domainObject.getId(), domainObject.getString("name"), getContentAsBase64(domainObject.getId())));
                }
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error get signed data", ex);
        }
    }

    private boolean isInhereFrom(String child, String parent) {
        boolean result = false;

        String checkType = child;
        while (checkType != null && !result) {
            result = checkType.equalsIgnoreCase(parent);
            checkType = configurationExplorer.getDomainObjectParentType(checkType);
        }

        return result;
    }

    private String getContentAsBase64(Id id) throws IOException {
        return Base64.encodeBase64String(getAttachmentContent(id));
    }

    protected byte[] getAttachmentContent(Id attachmentId) throws IOException {
        InputStream contentStream = null;
        RemoteInputStream inputStream = null;
        try {
            inputStream = attachmentService.loadAttachment(attachmentId);
            contentStream = RemoteInputStreamClient.wrap(inputStream);
            ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = contentStream.read(buffer)) > 0) {
                attachmentBytes.write(buffer, 0, read);
            }
            return attachmentBytes.toByteArray();
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                inputStream.close(true);
            } catch (IOException ignoreEx) {
            }
        }
    }

}
