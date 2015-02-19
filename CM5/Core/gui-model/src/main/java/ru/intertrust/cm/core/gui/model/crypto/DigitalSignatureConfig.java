package ru.intertrust.cm.core.gui.model.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;

public class DigitalSignatureConfig implements Dto{
    private static final long serialVersionUID = -2903303949393022102L;
    private boolean canSigned;
    private CryptoSettingsConfig cryptoSettingsConfig;
    
    public boolean isCanSigned() {
        return canSigned;
    }

    public void setCanSigned(boolean canSigned) {
        this.canSigned = canSigned;
    }

    public CryptoSettingsConfig getCryptoSettingsConfig() {
        return cryptoSettingsConfig;
    }

    public void setCryptoSettingsConfig(CryptoSettingsConfig cryptoSettingsConfig) {
        this.cryptoSettingsConfig = cryptoSettingsConfig;
    }
}
