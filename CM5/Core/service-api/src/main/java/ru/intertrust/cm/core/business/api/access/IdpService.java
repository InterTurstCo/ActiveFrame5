package ru.intertrust.cm.core.business.api.access;

/**
 * Сервис интеграции с IDP
 */
public interface IdpService {
    /**
     * Создание пользователя
     * @param userInfo
     * @return
     */
    String createUser(UserInfo userInfo);

    /**
     * Обновление пользователя
     * @param userInfo
     * @return
     */
    String updateUser(UserInfo userInfo);

    /**
     * Удаление пользователя
     * @param unid
     */
    void deleteUser(String unid);

    /**
     * Получение пользователя по UNID. Если такого пользователя нет то формируется исключение
     * @param unid
     * @return
     */
    UserInfo getUserByUnid(String unid);

    /**
     * Поиск пользователя по username
     * @param userName
     * @return
     */
    UserInfo findUserByUserName(String userName);

    /**
     * Отключение пользователя
     * @param unid
     */
    void disableUser(String unid);

    /**
     * Включение пользователя
     * @param unid
     */
    void enableUser(String unid);

    /**
     * Получение конфигурации idp провайдера для интеграционных задач
     * @return
     */
    IdpConfig getConfig();
}
