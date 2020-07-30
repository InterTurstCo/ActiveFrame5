package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.Id;

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
    void insertAuthenticationInfoAndRole(AuthenticationInfoAndRole authenticationInfo, Id userGroupId);

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

    /**
     * Возвращает тайм-аут сессии
     * @return таймаут сессии в минутах
     */
    Integer getSessionTimeout();

}
