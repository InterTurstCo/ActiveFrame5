package ru.intertrust.cm.core.gui.impl.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Фильтр проверки билета
 * @author larin
 *
 */
public class CheckTicketFilter implements Filter{
    private static final Logger logger = LoggerFactory.getLogger(CheckTicketFilter.class);
    private CurrentUserAccessor currentUserAccessor;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(filterConfig.getServletContext());
        this.currentUserAccessor = ctx.getBean(CurrentUserAccessor.class);        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        String ticket = httpRequest.getHeader("Ticket");
        String authorization = httpRequest.getHeader("Authorization");
        if (ticket != null){
            currentUserAccessor.setTicket(ticket);
            chain.doFilter(request, response);
            currentUserAccessor.cleanTicket();
        }else if(authorization != null) {
            if (login(httpRequest, authorization)) {
                chain.doFilter(request, response);
                httpRequest.logout();
            }else{
                unauthorized(httpResponse);
            }
        }else{
            logger.error("Call WS deny. Ticket or Authorization header is required");
            unauthorized(httpResponse);
        }
    }

    private void unauthorized(HttpServletResponse httpResponse){
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setHeader("WWW-Authenticate", "BASIC realm=\"AF5 Login\"");
    }

    @Override
    public void destroy() {
    }

    private boolean login(HttpServletRequest request, String authorization) throws UnsupportedEncodingException {
        final String encodedUserPassword = authorization.replaceFirst("Basic ", "");
        String usernameAndPassword = null;
            byte[] decodedBytes = Base64.decodeBase64(encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");

        String[] authData = usernameAndPassword.split(":");
        if (authData.length < 2){
            logger.error("authorization is invalid", authorization);
            return false;
        }
        final String username = authData[0];
        final String password = authData[1];
        try {
            request.login(username, password);
            return true;
        }catch(ServletException ex){
            logger.error("login error", ex);
            return false;
        }
    }
}
