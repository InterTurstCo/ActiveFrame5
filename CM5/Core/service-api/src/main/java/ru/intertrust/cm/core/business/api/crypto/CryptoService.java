package ru.intertrust.cm.core.business.api.crypto;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedResult;

public interface CryptoService extends CryptoTool{
    
    public interface Remote extends CryptoService {
    }
    
    CryptoSettingsConfig getCryptoSettingsConfig();
    
    SignedData getSignedData(Id rootId);
    
    void saveSignedResult(SignedResult signedResult);
}
