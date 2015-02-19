package ru.intertrust.cm.core.business.impl.crypto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.CollectorSettings;

@Root(name = "domain-object-store-signature")
public class DomainObjectStoreSignatureSettings implements CollectorSettings {
    private static final long serialVersionUID = 4651553197572480319L;

    @Attribute(name = "signature-store-type-name", required=true)
    private String signatureStoreTypeName;

    @Attribute(name = "signed-attachment-field-name", required=true)
    private String signedAttachmentFieldName;

    @Attribute(name = "signature-field-name", required=true)
    private String signatureFieldName;

    public String getSignatureStoreTypeName() {
        return signatureStoreTypeName;
    }

    public void setSignatureStoreTypeName(String signatureStoreTypeName) {
        this.signatureStoreTypeName = signatureStoreTypeName;
    }

    public String getSignedAttachmentFieldName() {
        return signedAttachmentFieldName;
    }

    public void setSignedAttachmentFieldName(String signedAttachmentFieldName) {
        this.signedAttachmentFieldName = signedAttachmentFieldName;
    }

    public String getSignatureFieldName() {
        return signatureFieldName;
    }

    public void setSignatureFieldName(String signatureFieldName) {
        this.signatureFieldName = signatureFieldName;
    }
}
