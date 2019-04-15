package ru.intertrust.cm.core.gui.api.server.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;

/**
 * Интерфейс аутентификации пользователя
 * @author larin
 *
 */
public interface AuthenticationProvider {
    
    /**
     * Ссылка на страницу логина
     * @return
     */
    String getLoginPage();

    /**
     * Ссылка на иконку, для ссылки на страницу логина, для отображения из страницы логина по умолчанию
     * @return
     */
    String getLoginImageUrl(); 
       
    /**
     * Выполняет аутентификацию пользователя
     * @return
     */
    UserCredentials login(HttpServletRequest request, HttpServletResponse response);
}
