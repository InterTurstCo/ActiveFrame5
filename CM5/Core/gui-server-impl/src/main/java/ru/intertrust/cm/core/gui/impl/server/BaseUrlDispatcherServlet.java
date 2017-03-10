package ru.intertrust.cm.core.gui.impl.server;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.client.support.HttpRequestWrapper;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Этот клас просто диспатчит нас на BusinessUniverse.html при любом URL вида
 * http://context_path/base_url/something Доступа к конфигурации мы тут не имеем так что просто
 * передаем через атрибут сессии URL в класс BusinessUniverse и там уже парсим имя приложения
 *
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 08.09.2015
 */
@WebServlet
public class BaseUrlDispatcherServlet extends HttpServlet {
    private static final String BU_PAGE = "/BusinessUniverse.html";

    @EJB
    private ConfigurationService configurationService;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        final String baseUrl = businessUniverseConfig.getBaseUrlConfig().getValue();

        HttpSession session = request.getSession(false);
        if (session.getAttribute("uri") == null) {
            session.setAttribute("uri", request.getRequestURI());
        }
        String targetPage = request.getRequestURI().substring(request.getRequestURI().indexOf('/', 2) + 1);

        //response.sendRedirect(request.getContextPath() + BU_PAGE +((request.getQueryString()!=null)?"?"+request.getQueryString():""));
        if (request.getRequestURI().toString().contains(BU_PAGE) ||
                !request.getRequestURI().toString().contains(".")) {
            forward(BU_PAGE + ((request.getQueryString() != null) ? "?" + request.getQueryString() : ""),
                    request, response);

        } else {
            String otherLink = request.getRequestURI().toString().substring(
                    request.getRequestURI().toString().indexOf(baseUrl) + baseUrl.length());
            final String xgwt = request.getHeader("X-GWT-Module-Base");
            HttpServletRequestWrapper myWrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String hName) {
                    if (hName.equals("X-GWT-Module-Base")) {
                        return xgwt.replace(baseUrl + "/", "");
                    } else {
                        return super.getHeader(hName);
                    }
                }
            };
            forward(otherLink, myWrapper, response);
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        final String baseUrl = businessUniverseConfig.getBaseUrlConfig().getValue();
        final String otherLink = request.getRequestURI().toString().substring(
                request.getRequestURI().toString().indexOf(baseUrl) + baseUrl.length());
        final String xgwt = request.getHeader("X-GWT-Module-Base");
        HttpServletRequestWrapper myWrapper = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String hName) {
                if (hName.equals("X-GWT-Module-Base")) {
                    return xgwt.replace(baseUrl + "/", "");
                } else {
                    return super.getHeader(hName);
                }
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                String requestStr = IOUtils.toString(request.getInputStream());
                requestStr = requestStr.replace(baseUrl + "/", "");
                final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestStr.getBytes(StandardCharsets.UTF_8));
                ServletInputStream servletInputStream = new ServletInputStream() {
                    public int read() throws IOException {
                        return byteArrayInputStream.read();
                    }
                };
                return servletInputStream;
            }
        };

        forward(otherLink, myWrapper, response);
    }

    private void forward(String url, HttpServletRequest aRequest, HttpServletResponse aResponse
    ) throws ServletException, IOException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
        dispatcher.forward(aRequest, aResponse);
    }
}
