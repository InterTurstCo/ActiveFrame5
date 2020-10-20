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
     * Получениеи публичного ключа области аутентификации
     * @return
     */
    String getRealmPublicKey();

    /**
     * Получение идентификатора клиента
     * @return
     */
    String getClientId();

    /**
     * Получение логина администратора для модификации настроек IDP сервера из системы
     * @return
     */
    String getAdminLogin();

    /**
     * Получение пароля администратора для модификации настроек IDP сервера из системы
     * @return
     */
    String getAdminPassword();

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
