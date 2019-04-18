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
     * Проверка вхождения пользователя в статическую группу Superusers и кэширование этого флага.
     * @param personId идентификатор пользователя
     * @return true если пользователь входит в группу Superusers
     */
    boolean isPersonSuperUser(Id personId);
    
    /**
     * Проверка вхождения пользователя в статическую группу Administrators и кэширование этого флага
     * @param personId идентификатор пользователя
     * @return true если пользователь входит в группу Administrators.
     */
    boolean isAdministrator(Id personId);
    
    /**
     * Проверка вхождения пользователя в статическую группу InfoSecAuditor и кэширование этого флага
     * @param personId
     * @return
     */
    boolean isInfoSecAuditor(Id personId);
    
    /**
     * Очищает глобальный кещ.
     */
    void cleanCache();
}
