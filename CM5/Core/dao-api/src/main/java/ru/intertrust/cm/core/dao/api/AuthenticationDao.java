package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;

/**
 * DAO для работы с  системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public interface AuthenticationDao {

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

}
