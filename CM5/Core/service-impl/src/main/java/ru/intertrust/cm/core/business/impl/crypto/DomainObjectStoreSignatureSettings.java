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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainObjectStoreSignatureSettings that = (DomainObjectStoreSignatureSettings) o;

        if (signatureStoreTypeName != null ? !signatureStoreTypeName.equals(that.signatureStoreTypeName) : that.signatureStoreTypeName != null)
            return false;
        if (signedAttachmentFieldName != null ? !signedAttachmentFieldName.equals(that.signedAttachmentFieldName) : that.signedAttachmentFieldName != null)
            return false;
        if (signatureFieldName != null ? !signatureFieldName.equals(that.signatureFieldName) : that.signatureFieldName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return signatureFieldName != null ? signatureFieldName.hashCode() : 0;
    }
}
