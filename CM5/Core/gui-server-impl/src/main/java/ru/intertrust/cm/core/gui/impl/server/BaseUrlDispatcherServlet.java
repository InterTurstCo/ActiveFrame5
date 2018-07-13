package ru.intertrust.cm.core.gui.impl.server;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
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
  private static final String ATTRIBUTE_URI = "uri";

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException, ServletException {
    HttpSession session = request.getSession(false);
    if (session.getAttribute(ATTRIBUTE_URI) == null
        || (!session.getAttribute(ATTRIBUTE_URI).toString().equals(request.getRequestURI())
        && !request.getRequestURI().contains(".") && !request.getRequestURI().contains("js"))
        )
      session.setAttribute(ATTRIBUTE_URI, request.getRequestURI());
    //response.sendRedirect(request.getContextPath() + BU_PAGE +((request.getQueryString()!=null)?"?"+request.getQueryString():""));

    // первая версия костыля для CMFIVE-18382 (забраковано)
//        if (request.getRequestURL().toString().contains("app/attachment-download")) {
//            response.sendRedirect(request.getContextPath() + "/attachment-download" +((request.getQueryString()!=null)?"?"+request.getQueryString():""));
//            return;
//        }

    if (!request.getRequestURL().toString().substring(request.getRequestURL().toString().indexOf("/", 8)).contains(".")
        && !request.getRequestURL().toString().contains("attachment-download")  // - вторая версия костыля для CMFIVE-18382
        ) {
      request.getRequestDispatcher(BU_PAGE).forward(request, response);
    } else {
      request.getRequestDispatcher(request.getRequestURI().substring(request.getRequestURI().
          indexOf(request.getAttribute(VirtualAppFilter.APP_NAME).toString())
          + request.getAttribute(VirtualAppFilter.APP_NAME).toString().length())).forward(request, response);
    }
  }

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
    String csvPath = "/json-export-to-csv";
    String csvFilter = request.getAttribute("Application-Name")+csvPath;
    if (request.getRequestURI().contains(csvFilter)){
      request.setAttribute("Json Payload",getJson(request));
    }

      request.getRequestDispatcher(request.getRequestURI().substring(request.getRequestURI().
          indexOf(request.getAttribute(VirtualAppFilter.APP_NAME).toString())
          + request.getAttribute(VirtualAppFilter.APP_NAME).toString().length())).forward(request, response);

  }

  public String getJson(HttpServletRequest request){
    StringBuffer jb = new StringBuffer();
    String line = null;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
        jb.append(line);
    } catch (Exception e) { /*report an error*/ }
    return jb.toString();
  }
}
