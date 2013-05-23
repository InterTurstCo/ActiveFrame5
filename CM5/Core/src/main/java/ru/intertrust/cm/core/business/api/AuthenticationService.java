package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;

/**
 * Сервис для работы с бизнес-объектом Person.
 * 
 * @author atsvetkov
 * 
 */
public interface AuthenticationService {

    /**
     * Добавление пользователя.
     * @param authenticationInfo {@link AuthenticationInfo} пользователь для сохранения
     * @return
     */
    void insertAuthenticationInfo(AuthenticationInfo authenticationInfo);

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    boolean existsAuthenticationInfo(String login);

}
