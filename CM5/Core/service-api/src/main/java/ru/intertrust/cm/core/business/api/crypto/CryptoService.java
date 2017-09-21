package ru.intertrust.cm.core.business.api.crypto;

import java.io.InputStream;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;

/**
 * Крипто сервис. Серверный компонент для обеспечения работы функций формирования и проверки электронной подписи
 * @author larin
 *
 */
public interface CryptoService{
    public static final String TIME_STAMP_SERVER = "TIME_STAMP_SERVER"; 
    public static final String HASH_ALGORITHM = "HASH_ALGORITHM"; 
    public static final String SIGNATURE_TYPE = "SIGNATURE_TYPE"; 
    public static final String HASH_ON_SERVER = "HASH_ON_SERVER";   
    
    public static final String HASH_ALGORITHM_GOST_3411 = "GOST_3411";   
    public static final String HASH_ALGORITHM_GOST_3411_2012_256 = "GOST_3411_2012_256";   
    public static final String HASH_ALGORITHM_GOST_3411_2012_512 = "GOST_3411_2012_512";  
    
    public interface Remote extends CryptoService {
    }
    
    /**
     * Проверка усовершенствованной ЭП включеннной в документ
     * @param inputStream поток содержащий документ со встроенной электронной подписью
     * @return
     */
    VerifyResult verify(InputStream document);
    
    /**
     * Проверка Усовершенствованной ЭП не включенной в документ
     * @param document поток содержащий документ
     * @param signature электронная подпись
     * @return
     */
    VerifyResult verify(InputStream document, byte[] signature);
    
    /**
     * Проверка стандартной ЭП
     * @param document поток содержащий документ
     * @param signature электронная подпись
     * @param signerSertificate сертификат подписавшено документ в формате DER
     * @return
     */
    VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate);

    /**
     * Формирование hash переданного документа
     * @param document
     * @return
     */
    byte[] hash(InputStream document);
    
    /**
     * Получение конфигураций из глобальных настроек крипто модуля
     * @return
     */
    CryptoSettingsConfig getCryptoSettingsConfig();
    
    /**
     * Получение контента для ЭП по идентификатору
     * @param id
     * @return
     */
    SignedDataItem getContentForSignature(Id id);
    
    /**
     * Получение идентификаторов связанных доменных объетов контенты которых надо подписать.
     * Например при подписание документа реально подписываются вложения к этому документу. 
     * Тоесть при передаче на вход функции идентификатора документа в возвращаемом результате будут идентификаторы вложений к этому документу 
     * @param rootId
     * @return
     */
    List<Id> getBatchForSignature(Id rootId);
    
    /**
     * Cохранение электронной подписи в хранилище
     * @param signedResult
     */
    void saveSignedResult(SignedResultItem signedResult);
    
    /**
     * Проверка ЭП вложения (ДО наследника attachment)
     * @param documentId идентификатор документа
     * @return
     */
    List<DocumentVerifyResult> verify(Id documrntId);

}
