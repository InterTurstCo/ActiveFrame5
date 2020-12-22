package ru.intertrust.cm.core.business.api.access;

/**
 * Настройки IDP сервера
 */
public interface IdpConfig {

    /**
     * Получение URL IDP сервера
     * @return
     */
    String getServerUrl();

    /**
     * Получение области аутентификации
     * @return
     */
    String getRealm();

    /**
     * Получение идентификатора клиента
     * @return
     */
    String getClientId();

    /**
     * Получение приватного ключа административного клиента
     * @return
     */
    String getAdminSecret();

    /**
     * Получение идентификатора административного клиента
     * @return
     */
    String getAdminClientId();

    /**
     * Возвращает флаг проверки SSL сервера IDP
     * @return
     */
    boolean isDisableTrustManager();

    /**
     * Получение пути файла хранилища сертификатов IDP сервера в случае использования https на IDP
     * @return
     */
    String getTruststore();

    /**
     * Получение пароля к хранилищу сертификатов IDP сервера в случае использования https на IDP
     * @return
     */
    String getTruststorePassword();

    /**
     * Возвращает флаг включена ли аутентификация на IDP сервере
     * @return
     */
    boolean isIdpAuthentication();
}
