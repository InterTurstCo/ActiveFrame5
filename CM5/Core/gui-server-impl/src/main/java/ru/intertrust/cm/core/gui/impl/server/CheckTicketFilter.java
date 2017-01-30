package ru.intertrust.cm.core.gui.impl.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Фильтр проверки билета
 * @author larin
 *
 */
public class CheckTicketFilter implements Filter{

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
        String ticket = httpRequest.getHeader("Ticket"); 
        if (ticket != null){
            currentUserAccessor.setTicket(ticket);
            chain.doFilter(request, response);
        }else{
            throw new FatalException("REST call without ticket");
        }
    }

    @Override
    public void destroy() {
    }

}
