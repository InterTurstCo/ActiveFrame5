package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;

/**
 * Сервис для работы с доменным объектом Authentication Info.
 *
 * @author atsvetkov
 *
 */
public interface AuthenticationService {

    /**
     * Добавление пользователя.
     * @param authenticationInfo {@link AuthenticationInfoAndRole} пользователь для сохранения
     * @return
     */
    void insertAuthenticationInfoAndRole(AuthenticationInfoAndRole authenticationInfo);

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

}
