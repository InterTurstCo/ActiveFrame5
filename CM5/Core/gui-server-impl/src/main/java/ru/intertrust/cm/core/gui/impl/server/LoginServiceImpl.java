package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.model.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


/**
 * @author Denis Mitavskiy
 *         Date: 01.08.13
 *         Time: 13:17
 */
public class LoginServiceImpl implements LoginService {
    /**
     * Атрибут, в котором хранятся данные авторизованного пользователя
     */
    public static final String USER_CREDENTIALS_SESSION_ATTRIBUTE = "_USER_CREDENTIALS";

    @Override
    public void login(HttpServletRequest request, UserCredentials credentials) throws AuthenticationException {
        UserUidWithPassword uidWithPassword = (UserUidWithPassword) credentials;
        String userUid = uidWithPassword.getUserUid();
        String password = uidWithPassword.getPassword();
        try {
            request.login(userUid, password);
            request.getSession().setAttribute(USER_CREDENTIALS_SESSION_ATTRIBUTE, credentials);
            request.logout();
        } catch (ServletException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            request.logout();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
