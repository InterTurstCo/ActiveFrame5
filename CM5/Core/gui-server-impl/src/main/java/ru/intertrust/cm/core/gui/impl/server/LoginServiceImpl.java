package ru.intertrust.cm.core.gui.impl.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.model.AuthenticationException;
import ru.intertrust.cm.core.util.SpringApplicationContext;


/**
 * @author Denis Mitavskiy
 *         Date: 01.08.13
 *         Time: 13:17
 */
public class LoginServiceImpl implements LoginService {

    @Override
    public void login(HttpServletRequest request, UserCredentials credentials) throws AuthenticationException {
        UserUidWithPassword uidWithPassword = (UserUidWithPassword) credentials;
        String userUid = uidWithPassword.getUserUid();
        String password = uidWithPassword.getPassword();
        try {
            request.login(userUid, password);
            request.getSession().setAttribute(USER_CREDENTIALS_SESSION_ATTRIBUTE, credentials);
            if (JeeServerFamily.determine(request.getServletContext()) == JeeServerFamily.JBOSS) {
                request.logout();
            }
            getEventLogService().logLogInEvent(userUid, request.getRemoteAddr(), true);
        } catch (ServletException e) {
            getEventLogService().logLogInEvent(userUid, request.getRemoteAddr(), false);
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
     private EventLogService getEventLogService(){
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(EventLogService.class);
    }
}
