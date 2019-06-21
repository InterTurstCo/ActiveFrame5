package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.model.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 * Данный класс-служба содержит операции, относящиеся к аутентификации и авторизации клиента приложения
 *
 * Author: Denis Mitavskiy
 * Date: 01.08.13
 * Time: 13:20
 */
public interface LoginService {
    /**
     * Атрибут, в котором хранятся данные авторизованного пользователя
     */
    public static final String USER_CREDENTIALS_SESSION_ATTRIBUTE = "_USER_CREDENTIALS";
    
    /**
     * Атрибут сесcии в которой передается ip клиента выполнившего logout
     */
    public static final String LOGOUT_IP_SESSION_ATTRIBUTE = "_LOGOUT.IP";    
    
    /**
     * Осуществляет вход пользователя в систему, проверяя соответствие учётных данных
     * @param credentials учётные данные пользователя
     * @throws ru.intertrust.cm.core.model.AuthenticationException, если по учётным данным вход в систему запрещён
     */
    public void login(HttpServletRequest request, UserCredentials credentials) throws AuthenticationException;

    /**
     * Осуществляет выход пользователя из системы
     */
    public void logout(HttpServletRequest request);
}
