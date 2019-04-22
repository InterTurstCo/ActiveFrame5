package ru.intertrust.cm.core.gui.impl.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.gui.api.server.ApplicationSecurityManager;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.gui.api.server.authentication.AuthenticationProvider;
import ru.intertrust.cm.core.gui.api.server.authentication.SecurityConfig;
import ru.intertrust.cm.core.gui.api.server.extension.AuthenticationExtentionHandler;

/**
 * Фильтр HTTP запросов, осуществляющий аутентификацию пользователей
 *
 * @author Denis Mitavskiy Date: 09.07.13 Time: 18:30
 */
public class AuthenticationFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String AUTHENTICATION_SERVICE_ENDPOINT = "BusinessUniverseAuthenticationService";
    private static final String REMOTE = "/remote";
    private static final String AF5_STRONG_SECURITY_DOMAIN = "af5.strong.security.domain";

    private ExtensionService extensionService;

    @EJB
    private ConfigurationService configurationService;

    @EJB
    private ApplicationSecurityManager applicationSecurityManager;

    private List<AuthenticationProvider> authenticationProviders;

    private SecurityConfig securityConfig;

    private EventLogService eventLogService;

    /**
     * Домен безопасности для строгой непосредственной аутентификации
     */
    private String strongSecurityDomain;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(filterConfig.getServletContext());
        this.extensionService = ctx.getBean(ExtensionService.class);

        // Получение разрешенных способов аутентификации
        authenticationProviders = applicationSecurityManager.getAuthenticationProviders();

        securityConfig = applicationSecurityManager.getSecurityConfig();

        strongSecurityDomain = ctx.getEnvironment().getProperty(AF5_STRONG_SECURITY_DOMAIN);

        eventLogService = ctx.getBean(EventLogService.class);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String requestURI = request.getRequestURI();

        // происходит отображение формы логина, разрешить этот запрос
        if (isLoginPageRequest(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //Вызов точки расширения до аутентификации. Точка расширения может проставить атрибут LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE
        //И вызов диалога аутентификации не произойдет
        AuthenticationExtentionHandler authExtHandler = extensionService.getExtentionPoint(AuthenticationExtentionHandler.class, null);
        authExtHandler.onBeforeAuthentication(request, response);

        // Получение данных аутентификации из сесии
        UserCredentials credentials = (UserCredentials) session.getAttribute(LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE);

        // Способ аутентификации текущего пользователя
        String currentAuthenticationType = null;
        AuthenticationProvider currentAuthenticationProvider = null;
        
        // Если не аутентифицированы, или сессия завершена то выполняем аутентификацию 
        if (credentials == null || (credentials != null && session.isNew())) {

            // Поверяем разрешена ли basic аутентификация
            if (securityConfig.getActiveProviders().contains(ApplicationSecurityManager.BASIC_AUTHENTICATION_TYPE)) {
                String authCredentials = request.getHeader("Authorization");
                if (authCredentials != null && !authCredentials.trim().toString().isEmpty()) {

                    try {
                        final String encodedUserPassword = authCredentials.replaceFirst("Basic"
                                + " ", "");
                        String usernameAndPassword = null;
                        try {
                            byte[] decodedBytes = Base64.decodeBase64(
                                    encodedUserPassword);
                            usernameAndPassword = new String(decodedBytes, "UTF-8");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                        final String username = tokenizer.nextToken();
                        final String password = tokenizer.nextToken();

                        // Выполяем реальную аутентификациию
                        strongAuthentication(request, username, password);

                        credentials = new UserUidWithPassword(username, password);
                        currentAuthenticationType =ApplicationSecurityManager.BASIC_AUTHENTICATION_TYPE;
                    } catch (Exception e) {
                        // В случае ошибки basic аутентификации возвращаем 403
                        response.setStatus(403);
                        return;
                    }
                }
            }

            // Возможно мы пришли от формы логина
            if (securityConfig.getActiveProviders().contains(ApplicationSecurityManager.FORM_AUTHENTICATION_TYPE)) {
                UserUidWithPassword loginData = (UserUidWithPassword) session.getAttribute(ApplicationSecurityManager.LOGIN_FORM_DATA);
                if (loginData != null) {
                    session.setAttribute(ApplicationSecurityManager.LOGIN_FORM_DATA, null);
                    try {
                        // Выполяем реальную аутентификациию
                        strongAuthentication(request, loginData.getUserUid(), loginData.getPassword());

                        credentials = loginData;
                        currentAuthenticationType = ApplicationSecurityManager.FORM_AUTHENTICATION_TYPE;
                    } catch (LoginException | ServletException e) {
                        forwardToLogin(servletRequest, servletResponse, true);
                        return;
                    }
                }
            }

            if (credentials == null) {
                for (AuthenticationProvider authenticationProvider : authenticationProviders) {
                    credentials = authenticationProvider.login(request, response);
                    if (credentials != null) {
                        currentAuthenticationType = ApplicationSecurityManager.PROVIDER_AUTHENTICATION_TYPE;
                        currentAuthenticationProvider = authenticationProvider; 
                        break;
                    }
                }
            }

            if (credentials == null) {
                forwardToLogin(servletRequest, servletResponse, false);
                return;
            }
        }

        //Вызов точки расширения после аутентификации. Точки расширения могут сохранить данные аутентификации для каких то последующих их использования
        //Например для использования в SSO
        //authExtHandler.onAfterAuthentication(request, response, userUidWithPassword);

        if (request.getUserPrincipal() == null) { // just in case parallel thread logged in, but not logged out yet
            try {
                // Выполняем фейковую аутентификацию если задан strongSecurityDomain, или реальную аутентификацию если не задан
                if (strongSecurityDomain != null) {
                    request.login(credentials.getUserUid(), credentials.getUserUid());
                }else {
                    request.login(credentials.getUserUid(), ((UserUidWithPassword) credentials).getPassword());
                }
                session.setAttribute(LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE, credentials);
                session.setAttribute(ApplicationSecurityManager.HIDE_LOGOUT_BUTTON, isHideLogoutButton(currentAuthenticationType, currentAuthenticationProvider));
                eventLogService.logLogInEvent(credentials.getUserUid(), request.getRemoteAddr(), true);
            } catch (Exception ex) {
                forwardToLogin(servletRequest, servletResponse, true);
                eventLogService.logLogInEvent(credentials.getUserUid(), request.getRemoteAddr(), false);
            }
        } else {
            log.debug("User principal is already in request");
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            if (JeeServerFamily.isLogoutRequired(request)) {
                try {
                    if (strongSecurityDomain != null) {
                        request.logout();
                    }
                } catch (Exception e) {
                    log.error("request logout failed", e);
                }
            } else {
                log.debug("No user principal. Do NOT log out");
            }
        }
    }

    private boolean isHideLogoutButton(String authenticationType, AuthenticationProvider provider) {
        if (authenticationType.equals(ApplicationSecurityManager.FORM_AUTHENTICATION_TYPE)) {
            return false;
        }else if (authenticationType.equals(ApplicationSecurityManager.BASIC_AUTHENTICATION_TYPE)) {
            return true;
        }else {
            return provider.getLoginPage() == null;
        }
    }

    /**
     * Метод выполняет аутентификацию на строго настроенном домене безопасности.
     * Необходим для basic и form аутентификации. В случае если настроен
     * отдельный строгий домен безопасности то используем его, если не настроен
     * то используем домен безопасности по умолчанию
     * @param login
     * @param password
     * @throws LoginException
     * @throws ServletException
     */
    private void strongAuthentication(HttpServletRequest request, String login, String password) throws LoginException, ServletException {
        try {
            if (strongSecurityDomain != null) {
                // Выполяем реальную аутентификациию
                LoginContext lc = login(strongSecurityDomain, login, password);
                // Если все прошло успешно выполняем logout, потому что далее выполнится фейковая аутентификация
                lc.logout();
            } else {
                request.login(login, password);
                request.logout();
            }
        } catch (LoginException | ServletException ex) {
            eventLogService.logLogInEvent(login, request.getRemoteAddr(), false);
            throw ex;
        }
    }

    private boolean isLoginPageRequest(String requestUri) {
        return requestUri.contains(AUTHENTICATION_SERVICE_ENDPOINT);
    }

    private void forwardToLogin(ServletRequest servletRequest, ServletResponse servletResponse, boolean authenticationError)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // Проверка, возможно form аутентификация запрещена, тогда отображаем нет доступа

        if (securityConfig.getActiveProviders().contains(ApplicationSecurityManager.FORM_AUTHENTICATION_TYPE)) {
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
                    if (!"targetPage".equals(paramName) && !"authenticationError".equals(paramName)) {
                        String[] paramValues = parameterMap.get(paramName);
                        for (String value : paramValues) {
                            loginPath.append("&").append(paramName).append("=").append(value);
                        }
                    }
                }

                if (authenticationError) {
                    loginPath.append("&").append("authenticationError").append("=").append(true);
                }
            }
            ((HttpServletResponse) servletResponse).sendRedirect(loginPath.toString());
        } else {
            ((HttpServletResponse) servletResponse).sendRedirect(request.getContextPath() + "/AccessDeny.html");
        }
    }

    @Override
    public void destroy() {
    }

    public LoginContext login(final String securityDomain, final String login, final String password) throws LoginException {
        CallbackHandler cbh = new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        NameCallback nc = (NameCallback) callbacks[i];
                        nc.setName(login);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback pc = (PasswordCallback) callbacks[i];
                        pc.setPassword(password.toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                    }
                }
            }
        };

        LoginContext lc = new LoginContext(securityDomain, cbh);
        lc.login();

        log.debug("Login success {}", lc.getSubject());
        return lc;
    }
}
