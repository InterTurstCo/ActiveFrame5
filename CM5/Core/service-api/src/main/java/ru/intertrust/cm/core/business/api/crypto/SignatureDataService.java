package ru.intertrust.cm.core.business.api.crypto;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;

/**
 * Сервис подготовки вложений к ЭП
 * @author larin
 *
 */
public interface SignatureDataService {
    /**
     * Получение идентификаторов доменных объектов которые надо подписать в пачке, например все вложения к карточке документа
     * @param settings
     * @param rootId
     * @return
     */
    List<Id> getBatchForSignature(CollectorSettings settings, Id rootId);
    
    /**
     * Получение контента для подписи. Это может быть вложение, может xml представление карточки или что то иное
     * @param settings
     * @param id
     * @return
     */
    SignedDataItem getContentForSignature(CollectorSettings settings, Id id);    
}
