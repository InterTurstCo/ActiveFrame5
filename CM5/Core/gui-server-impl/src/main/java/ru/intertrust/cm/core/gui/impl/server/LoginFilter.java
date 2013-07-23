package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.impl.client.temp.vaadin.LoginDialog;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Фильтр HTTP запросов, осуществляющий аутентификацию пользователей
 * @author Denis Mitavskiy
 *         Date: 09.07.13
 *         Time: 18:30
 */
public class LoginFilter implements Filter {
    public static final String LOGIN_DIALOG_SERVICE_MESSAGES_URL = LoginDialog.PATH + "/UIDL/";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        // todo to handle every request of UIDL ApplicationConnection.handleUIDLMessage should be overwritten
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession();

        String requestURI = request.getRequestURI();
        if (requestURI.endsWith(LOGIN_DIALOG_SERVICE_MESSAGES_URL)) { // происходит авторизация. разрешить этот запрос
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        UserCredentials credentials = (UserCredentials) session.getAttribute(GuiServiceImpl.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        if (credentials == null) {
            forwardToLogin(servletRequest, servletResponse);
            return;
        }

        UserUidWithPassword userUidWithPassword = (UserUidWithPassword) credentials;
        try {
            request.login(userUidWithPassword.getUserUid(), userUidWithPassword.getPassword());
            System.out.println("Filter");
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (ServletException e) {
            forwardToLogin(servletRequest, servletResponse);
        } finally {
            //request.logout();
        }
    }

    private void forwardToLogin(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        String loginPath = ((HttpServletRequest) servletRequest).getContextPath() + LoginDialog.PATH;
        ((HttpServletResponse) servletResponse).sendRedirect(loginPath);
    }

    @Override
    public void destroy() {
    }
}
