package ru.intertrust.cm.core.gui.impl.server;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;


/**
 * Этот клас просто диспатчит нас на BusinessUniverse.html при любом URL вида
 * http://context_path/base_url/something Доступа к конфигурации мы тут не имеем так что просто
 * передаем через атрибут сессии URL в класс BusinessUniverse и там уже парсим имя приложения
 *
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 08.09.2015
 */
public class BaseUrlDispatcherServlet extends HttpServlet {
    private static final String BU_PAGE = "/BusinessUniverse.html";

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        session.setAttribute( "uri", request.getRequestURI());
        response.sendRedirect(request.getContextPath() + BU_PAGE +((request.getQueryString()!=null)?"?"+request.getQueryString():""));


    }


}
