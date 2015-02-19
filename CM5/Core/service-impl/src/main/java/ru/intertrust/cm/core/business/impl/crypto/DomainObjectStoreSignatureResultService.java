package ru.intertrust.cm.core.business.impl.crypto;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.SignatureResultService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;

public class DomainObjectStoreSignatureResultService implements SignatureResultService{
    @Autowired
    private CrudService crudService;

    @Override
    public void saveSignatureresult(CollectorSettings settings, SignedResult signedResult) {
        DomainObjectStoreSignatureSettings domainObjectStoreSettings = (DomainObjectStoreSignatureSettings)settings;
        
        for (SignedResultItem resultItem : signedResult.getSignedResultItems()) {
            DomainObject signatureObject = crudService.createDomainObject(domainObjectStoreSettings.getSignatureStoreTypeName());
            signatureObject.setString(domainObjectStoreSettings.getSignatureFieldName(), resultItem.getSignature());
            signatureObject.setReference(domainObjectStoreSettings.getSignedAttachmentFieldName(), resultItem.getId());
            crudService.save(signatureObject);
        }
    }
}
