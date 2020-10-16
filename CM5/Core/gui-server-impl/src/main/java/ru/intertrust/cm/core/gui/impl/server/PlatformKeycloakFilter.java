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
import javax.servlet.http.HttpServletResponseWrapper;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PlatformKeycloakFilter extends KeycloakOIDCFilter {
    private boolean useIdp;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        ApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(filterConfig.getServletContext());

        String useIdpParamValue = ctx.getEnvironment().getProperty(AuthenticationFilter.USE_IDP_PARAMETER);
        useIdp = useIdpParamValue == null ? false : Boolean.parseBoolean(useIdpParamValue);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (useIdp) {
            // Подкладываем свой FilterChain, чтобы можно было перехватить вызов doFilter и произвести фейковую аутентификацию
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
            // Выполняем фейковую аутентификацию, для того чтоб работали EJB
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            LoginContext lc = login(httpRequest.getUserPrincipal().getName(), httpRequest.getUserPrincipal().getName());

            origChain.doFilter(request, response);

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
