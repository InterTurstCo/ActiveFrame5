package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;

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
public class AuthenticationFilter implements Filter {
    public static final String AUTHENTICATION_SERVICE_ENDPOINT = "BusinessUniverseAuthenticationService";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession();

        String requestURI = request.getRequestURI();
        if (isLoginPageRequest(requestURI)) { // происходит авторизация. разрешить этот запрос
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        UserCredentials credentials = (UserCredentials) session.getAttribute(
                LoginServiceImpl.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        if (credentials == null) {
            forwardToLogin(servletRequest, servletResponse);
            return;
        }
        UserUidWithPassword userUidWithPassword = (UserUidWithPassword) credentials;
        try {
            if (request.getUserPrincipal() == null) { // just in case parallel thread logged in, but not logged out yet
                request.login(userUidWithPassword.getUserUid(), userUidWithPassword.getPassword());
            }
            if (request.getUserPrincipal() != null) {
                request.logout();
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (ServletException e) {
            forwardToLogin(servletRequest, servletResponse);
        }
    }

    private boolean isLoginPageRequest(String requestUri) {
        return requestUri.contains(AUTHENTICATION_SERVICE_ENDPOINT);
    }

    private void forwardToLogin(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        String loginPath = ((HttpServletRequest) servletRequest).getContextPath() + "/Login.html";
        ((HttpServletResponse) servletResponse).sendRedirect(loginPath);
    }

    @Override
    public void destroy() {
    }
}
