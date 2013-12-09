package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Глобальный кеш. Кеширует информацию о пользователях и их вхождении в статическую группу Superusers.
 * @author atsvetkov
 */
public interface UserGroupGlobalCache {

    /**
     * Возвращает иденитфикатор пользователя по его логину.
     * @param login логин пользователя
     * @return иденитфикатор пользователя
     */
    Id getUserIdByLogin(String login);
    
    /**
     * Кеширует вхождение пользователя в статическую групру Superusers
     * @param personId идентификатор пользователя
     * @return true если пользователь входит в группу Superusers
     */
    boolean isPersonSuperUser(Id personId);
    
    /**
     * Очищает глобальный кещ.
     */
    void cleanCache();
}
