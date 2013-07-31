package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.config.model.NavigationConfig;
import ru.intertrust.cm.core.model.AuthenticationException;

/**
 * Данный класс-служба содержит операции, относящиеся к клиентскому приложению. Клиентское приложение может быть
 * сконфигурировано по-разному для различных ролей или конечных пользователей. Все методы данной службы учитывают
 * данный факт прозрачным образом, получая информацию о текущем пользователе (и его роли) из контекста выполнения.
 *
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:12
 */
public interface GuiService {
    /**
     * Осуществляет вход пользователя в систему, проверяя соответствие учётных данных
     * @param credentials учётные данные пользователя
     * @throws AuthenticationException, если по учётным данным вход в систему запрещён
     */
    public void login(UserCredentials credentials) throws AuthenticationException;

    /**
     * Осуществляет выход пользователя из системы
     */
    public void logout();

    /**
     * Возвращает конфигурацию панели навигации.
     * @return конфигурацию панели навигации
     */
    NavigationConfig getNavigationConfiguration();
}
