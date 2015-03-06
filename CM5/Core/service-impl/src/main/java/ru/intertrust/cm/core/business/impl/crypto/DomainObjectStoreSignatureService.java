package ru.intertrust.cm.core.business.impl.crypto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.SignatureStorageService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;

public class DomainObjectStoreSignatureService implements SignatureStorageService {
    @Autowired
    private CrudService crudService;
    @Autowired
    private CollectionsService collectionService;

    @Override
    public List<SignedResultItem> loadSignature(CollectorSettings settings, Id documentId) {
        DomainObjectStoreSignatureSettings domainObjectStoreSettings = (DomainObjectStoreSignatureSettings) settings;
        List<SignedResultItem> result = new ArrayList<SignedResultItem>();
        String query =
                "select * from " + domainObjectStoreSettings.getSignatureStoreTypeName() + " where " + domainObjectStoreSettings.getSignedAttachmentFieldName()
                        + " = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(documentId));
        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        for (IdentifiableObject identifiableObject : collection) {
            result.add(new SignedResultItem(documentId, identifiableObject.getString(domainObjectStoreSettings.getSignatureFieldName())));
        }
        return result;
    }

    @Override
    public void saveSignature(CollectorSettings settings, SignedResultItem signedResult) {
        DomainObjectStoreSignatureSettings domainObjectStoreSettings = (DomainObjectStoreSignatureSettings) settings;
        DomainObject signatureObject = crudService.createDomainObject(domainObjectStoreSettings.getSignatureStoreTypeName());
        signatureObject.setString(domainObjectStoreSettings.getSignatureFieldName(), signedResult.getSignature());
        signatureObject.setReference(domainObjectStoreSettings.getSignedAttachmentFieldName(), signedResult.getId());
        crudService.save(signatureObject);
    }
}
