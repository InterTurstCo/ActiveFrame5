package ru.intertrust.cm.core.config.crypto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.CollectorSettings;

@Root(name = "cades-crypto-settings-config")
public class CAdESCryptoSettingsConfig implements CollectorSettings {
    private static final long serialVersionUID = -6823371274654533928L;
    public static final String CADES_X_SIGNATURE_TYPE = "CAdES-X";
    public static final String CADES_BES_SIGNATURE_TYPE = "CAdES-BES";

    @Attribute(name = "ts-address", required = false)
    private String tsAddress;

    @Attribute(name = "hash-algorithm", required = false)
    private String hashAlgorithm;

    @Attribute(name = "signature-type", required = false)
    private String signatureType;

    public String getTsAddress() {
        return tsAddress;
    }

    public void setTsAddress(String tsAddress) {
        this.tsAddress = tsAddress;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hashAlgorithm == null) ? 0 : hashAlgorithm.hashCode());
        result = prime * result + ((signatureType == null) ? 0 : signatureType.hashCode());
        result = prime * result + ((tsAddress == null) ? 0 : tsAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CAdESCryptoSettingsConfig other = (CAdESCryptoSettingsConfig) obj;
        if (hashAlgorithm == null) {
            if (other.hashAlgorithm != null)
                return false;
        } else if (!hashAlgorithm.equals(other.hashAlgorithm))
            return false;
        if (signatureType == null) {
            if (other.signatureType != null)
                return false;
        } else if (!signatureType.equals(other.signatureType))
            return false;
        if (tsAddress == null) {
            if (other.tsAddress != null)
                return false;
        } else if (!tsAddress.equals(other.tsAddress))
            return false;
        return true;
    }
}
