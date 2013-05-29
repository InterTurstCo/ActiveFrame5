package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;

/**
 * DAO для работы с  системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public interface AuthenticationDAO {

    /**
     * Добавление пользователя в базу данных
     * @param authenticationInfo {@link AuthenticationInfo}
     * @return
     */
    int insertAuthenticationInfo(AuthenticationInfo authenticationInfo);

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

}
