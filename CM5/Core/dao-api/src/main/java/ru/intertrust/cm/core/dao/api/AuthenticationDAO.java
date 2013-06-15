package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;

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
     * Добавление роли в базу данных
     * @param authenticationInfo {@link AuthenticationInfo}
     * @return
     */
    int insertRole(BusinessObject role);

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

}
