package ru.intertrust.cm.core.business.impl.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.model.FatalException;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

public class CurrentAttachmentSignatureDataService implements SignatureDataService {
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private CrudService crudService;

    protected InputStream getAttachmentStream(Id attachmentId) throws IOException {
        RemoteInputStream inputStream = attachmentService.loadAttachment(attachmentId);
        InputStream contentStream = RemoteInputStreamClient.wrap(inputStream);
        return contentStream;
    }

    @Override
    public List<Id> getBatchForSignature(CollectorSettings settings, Id rootId) {
        return Collections.singletonList(rootId);
    }

    @Override
    public SignedDataItem getContentForSignature(CollectorSettings settings, Id id) {
        try {
            DomainObject domainObject = crudService.find(id);
            return new SignedDataItem(domainObject.getId(), domainObject.getString("name"), getAttachmentStream(domainObject.getId()));
        } catch (Exception ex) {
            throw new FatalException("Error get signed data", ex);
        }
    }
}
