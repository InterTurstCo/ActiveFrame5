package ru.intertrust.cm.core.business.api.crypto;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;

public interface CryptoService extends CryptoTool{
    
    public interface Remote extends CryptoService {
    }
    
    CryptoSettingsConfig getCryptoSettingsConfig();
    
    SignedDataItem getContentForSignature(Id id);
    
    List<Id> getBatchForSignature(Id rootId);
    
    void saveSignedResult(SignedResultItem signedResult);
    
    /**
     * Проверка ЭП вложения (ДО наследника attachment)
     * @param documentId идентификатор документа
     * @return
     */
    List<DocumentVerifyResult> verify(Id documrntId);

}
