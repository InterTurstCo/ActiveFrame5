package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Предоставляет доступ к текущему пользователю системы.
 *
 * @author atsvetkov
 */
public interface CurrentUserAccessor {

    String INITIAL_DATA_LOADING = "InitialDataLoading";

    /**
     * @return логин текущего пользователя
     */
    String getCurrentUser();

    /**
     * @return идентификатор текущего пользователя.
     */
    Id getCurrentUserId();

    /**
     * Установка билета пользователя
     */
    void setTicket(String ticket);

    /**
     * Завершение работы с использование билета.
     */
    void cleanTicket();

    /**
     * Получение информации о запросе данных
     */
    RequestInfo getRequestInfo();

    /**
     * Установка информации о запросе данных
     */
    void setRequestInfo(RequestInfo requestInfo);
}
