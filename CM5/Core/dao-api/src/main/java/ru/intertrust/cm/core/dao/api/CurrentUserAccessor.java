package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Предоставляет доступ к текущему пользователю системы.
 * @author atsvetkov
 *
 */
public interface CurrentUserAccessor {

    String INITIAL_DATA_LOADING = "InitialDataLoading";

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

    /**
     * Установка билета пользователя
     * @param ticket
     */
    void setTicket(String ticket);
}
