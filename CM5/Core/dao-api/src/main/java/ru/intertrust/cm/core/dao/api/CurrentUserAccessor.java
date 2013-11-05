package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Предоставляет доступ к текущему пользователю системы.
 * @author atsvetkov
 *
 */
public interface CurrentUserAccessor {

    /**
     * Возвращает логин текущего пользователя.
     * @return логин текущего пользователя
     */
    String getCurrentUser();
    
    /**
     * Возвращает идентификатор текущего пользователя.
     * @return идентификатор текущего пользователя.
     */
    Id getCurrentUserId();

}
