package ru.intertrust.cm.core.business.api.crypto;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;

/**
 * Сервис хранения ЭП
 * @author larin
 *
 */
public interface SignatureStorageService {
    /**
     * Сохранение ЭП
     * @param settings
     * @param signedResult
     */
    void saveSignature(CollectorSettings settings, SignedResultItem signedResult);
    
    /**
     * Загрузка всех ЭП документа
     * @param settings
     * @param id
     * @return
     */
    List<SignedResultItem> loadSignature(CollectorSettings settings, Id id);
}
