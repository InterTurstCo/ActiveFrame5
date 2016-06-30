package ru.intertrust.cm.core.gui.impl.server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 30.06.2016
 * Time: 12:15
 * To change this template use File | Settings | File and Code Templates.
 */
public class QuestionToHashFilter implements Filter {

    private static final String GWT_DEBUG_MODE = "gwt.codesvr";
    private static final String HASH = "#";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String url;
        String queryString;

        if (servletRequest instanceof HttpServletRequest) {
            url = ((HttpServletRequest)servletRequest).getRequestURL().toString();
            queryString = ((HttpServletRequest)servletRequest).getQueryString();
            if(queryString!=null && !queryString.contains(GWT_DEBUG_MODE)){
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                httpResponse.sendRedirect(url+HASH+queryString);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
