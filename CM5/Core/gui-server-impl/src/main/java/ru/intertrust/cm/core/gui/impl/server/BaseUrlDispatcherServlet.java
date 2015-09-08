package ru.intertrust.cm.core.gui.impl.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 08.09.2015
 */
public class BaseUrlDispatcherServlet extends HttpServlet {
    private static final String BU_PAGE = "/BusinessUniverse.html";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.sendRedirect(request.getContextPath() + BU_PAGE);
    }
}
