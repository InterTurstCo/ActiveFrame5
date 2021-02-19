package ru.intertrust.cm.core.gui.impl.server;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUid;
import ru.intertrust.cm.core.gui.api.server.LoginService;


public class PlatformKeycloakFilter extends KeycloakOIDCFilter {
    public static final Logger logger = LoggerFactory.getLogger(PlatformKeycloakFilter.class);
    private boolean useIdp;
    private PersonService personService;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        ApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(filterConfig.getServletContext());

        IdpAdminService idpAdminService = ctx.getBean(IdpAdminService.class);
        useIdp = idpAdminService.getConfig().isIdpAuthentication();

        personService = ctx.getBean(PersonService.class);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (useIdp) {
            // Подкладываем свой FilterChain, чтобы можно было перехватить вызов doFilter у родительского класса
            // и произвести фейковую аутентификацию
            PlatformFilterChainWrapper platformChain = new PlatformFilterChainWrapper(chain);
            super.doFilter(req, res, platformChain);
        }else{
            chain.doFilter(req, res);
        }
    }

    public class PlatformFilterChainWrapper implements FilterChain{
        private FilterChain origChain;

        public PlatformFilterChainWrapper(FilterChain chain){
            origChain = chain;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;

            // Выполняем фейковую аутентификацию, для того чтоб работали EJB
            LoginContext lc = login(httpRequest.getUserPrincipal().getName(), httpRequest.getUserPrincipal().getName());

            // Выполняем поиск пользователя с данным UNID
            DomainObject person = personService.findPersonByAltUid(httpRequest.getUserPrincipal().getName(), IdpAdminService.IDP_ALTER_UID_TYPE);
            if (person == null){
                httpResponse.setStatus(403);
                logger.warn("Not find person with alter uid = " + httpRequest.getUserPrincipal().getName());
            }else {
                // Сохраняем пользователя в сесии
                UserCredentials credentials = (UserCredentials) httpRequest.getSession().getAttribute(
                        LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE);
                if (credentials == null){
                    httpRequest.getSession().setAttribute(LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE,
                            new UserUid(person.getString("login")));
                }
                // Вызываем цепочку фильтров
                origChain.doFilter(request, response);
            }

            try {
                lc.logout();
            }catch (LoginException ignoreEx){
            }
        }
    }

    private LoginContext login(final String login, final String password) throws ServletException {
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

        try {
            LoginContext lc = new LoginContext("CM5", cbh);
            lc.login();
            return lc;
        }catch (LoginException ex){
            throw new ServletException(ex);
        }

    }
}
