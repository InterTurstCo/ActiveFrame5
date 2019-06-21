package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Слушатель окончания сессии
 */
public class LogOutListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {

        HttpSession session = httpSessionEvent.getSession();
        Object credentials = session.getAttribute(LoginServiceImpl.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        String logoutIp = (String)session.getAttribute(LoginServiceImpl.LOGOUT_IP_SESSION_ATTRIBUTE);

        if (credentials instanceof UserUidWithPassword){
            UserUidWithPassword userUidWithPassword = (UserUidWithPassword) credentials;
            String login = userUidWithPassword.getUserUid();
            ApplicationContext ctx = SpringApplicationContext.getContext();
            EventLogService eventLogService = ctx.getBean(EventLogService.class);
            eventLogService.logLogOutEvent(login, logoutIp);
        }

    }
}
