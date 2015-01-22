package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Фильтр HTTP запросов, осуществляющий аутентификацию пользователей
 * @author Denis Mitavskiy
 *         Date: 09.07.13
 *         Time: 18:30
 */
public class AuthenticationFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String AUTHENTICATION_SERVICE_ENDPOINT = "BusinessUniverseAuthenticationService";
    private static final String REMOTE = "/remote";

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
        if (request.getUserPrincipal() == null) { // just in case parallel thread logged in, but not logged out yet
            try {
                request.login(userUidWithPassword.getUserUid(), userUidWithPassword.getPassword());
                //System.out.println(Thread.currentThread().getId() + " => no user principal. Log in");
            } catch (ServletException e) {
                forwardToLogin(servletRequest, servletResponse);
            }
        } else {
            log.info(Thread.currentThread().getId() + " => user principal is already in request");
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            if (request.getUserPrincipal() != null) {
                try {
                    request.logout();
                    //System.out.println(Thread.currentThread().getId() + " => log out");
                } catch (ServletException e) {
                    log.error("request logout failed", e);
                }
            } else {
                log.info(Thread.currentThread().getId() + " => no user principal. Do NOT log out");
            }
        }

    }

    private boolean isLoginPageRequest(String requestUri) {
        return requestUri.contains(AUTHENTICATION_SERVICE_ENDPOINT);
    }

    private void forwardToLogin(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        StringBuilder loginPath = new StringBuilder(request.getContextPath()).append("/Login.html");
        String requestURI = request.getRequestURI();
        if (!requestURI.contains(REMOTE)) {
            String targetPage = request.getRequestURI().substring(request.getContextPath().length());
            if (targetPage.startsWith("/")) {
                targetPage = targetPage.substring(1);
            }
            loginPath.append("?targetPage=").append(targetPage);
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            for (String paramName : parameterMap.keySet()) {
                if (!"targetPage".equals(paramName)) {
                    String[] paramValues = parameterMap.get(paramName);
                    for (String value : paramValues) {
                        loginPath.append("&").append(paramName).append("=").append(value);
                    }
                }
            }
        }
        ((HttpServletResponse) servletResponse).sendRedirect(loginPath.toString());
    }

    @Override
    public void destroy() {
    }
}
