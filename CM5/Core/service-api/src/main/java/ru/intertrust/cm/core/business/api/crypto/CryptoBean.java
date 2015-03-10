package ru.intertrust.cm.core.business.api.crypto;

import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;

/**
 * Спринг интерфейс для выполнения криптографических операций. Может быть несколько реализация с помошью разных провайдеров
 * Конкретный бин подключается к приложению в global конфигурации
 * @author larin
 *
 */
public interface CryptoBean{
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

}
