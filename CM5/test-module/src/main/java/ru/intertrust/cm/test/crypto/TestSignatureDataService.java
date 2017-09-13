package ru.intertrust.cm.test.crypto;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.model.FatalException;

public class TestSignatureDataService implements SignatureDataService {
    @Autowired
    private CrudService crudService;


    @Override
    public List<Id> getBatchForSignature(CollectorSettings settings, Id rootId) {
        return Collections.singletonList(rootId);
    }

    @Override
    public SignedDataItem getContentForSignature(CollectorSettings settings, Id id) {
        try {
            DomainObject domainObject = crudService.find(id);
            String name1 = domainObject.getString("name1");
            return new SignedDataItem(domainObject.getId(), name1, new ByteArrayInputStream(name1.getBytes()));
        } catch (Exception ex) {
            throw new FatalException("Error get signed data", ex);
        }
    }
}
