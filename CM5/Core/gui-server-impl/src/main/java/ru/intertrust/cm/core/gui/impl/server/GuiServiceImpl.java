package ru.intertrust.cm.core.gui.impl.server;

import com.vaadin.server.VaadinService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.config.model.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.model.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
public class GuiServiceImpl implements GuiService {
    /**
     * Атрибут, в котором хранятся данные авторизованного пользователя
     */
    public static final String USER_CREDENTIALS_SESSION_ATTRIBUTE = "_USER_CREDENTIALS";

    @Override
    public void login(UserCredentials credentials) throws AuthenticationException {
        UserUidWithPassword uidWithPassword = (UserUidWithPassword) credentials;
        HttpServletRequest currentRequest = (HttpServletRequest) VaadinService.getCurrentRequest();
        String userUid = uidWithPassword.getUserUid();
        String password = uidWithPassword.getPassword();
        try {
            currentRequest.login(userUid, password);
            currentRequest.getSession().setAttribute(USER_CREDENTIALS_SESSION_ATTRIBUTE, credentials);
            currentRequest.logout();
        } catch (ServletException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public void logout() {
        try {
            HttpServletRequest currentRequest = (HttpServletRequest) VaadinService.getCurrentRequest();
            currentRequest.getSession().removeAttribute(USER_CREDENTIALS_SESSION_ATTRIBUTE);
            currentRequest.logout();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NavigationConfig getNavigationConfiguration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
