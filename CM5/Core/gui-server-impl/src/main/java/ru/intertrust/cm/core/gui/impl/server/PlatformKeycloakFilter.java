package ru.intertrust.cm.core.gui.impl.server;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
            super.doFilter(req, res, chain);
        }else{
            chain.doFilter(req, res);
        }
    }

}
