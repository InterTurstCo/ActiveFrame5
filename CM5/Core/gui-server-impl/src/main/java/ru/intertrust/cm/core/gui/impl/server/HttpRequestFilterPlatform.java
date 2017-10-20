package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.api.server.HttpRequestFilterUser;
import ru.intertrust.cm.core.gui.api.server.LoginService;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Ravil on 16.10.2017.
 */
@Stateless(name = "HttpRequestFilterUser")
@Local(HttpRequestFilterUser.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class HttpRequestFilterPlatform implements HttpRequestFilterUser {



    @Override
    public String getUserName(HttpServletRequest request) {
        UserCredentials credentials = (UserCredentials) request.getSession().getAttribute(
                LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        if (credentials != null)
            return ((UserUidWithPassword) credentials).getUserUid();
        else return null;
    }
}


