package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

import java.util.EnumSet;

/**
 * Добавляем к контекст мапинг для фильтра аутентификации.
 * Также добавляем мапинг для сервлета который будет форвардить на BusinessUniverse.html в случае если
 * пользователь ввел ссылку начинающуюся с base_url и прошел аутентификацию
 *
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 07.09.2015
 */
@WebListener
public class CustomContextListener implements ServletContextListener {

    private static final String LOGIN_FILTER = "LoginFilter";
    private static final String URL_WILDCARD = "/*";
    private static final String BASE_URL_SERVLET = "Base Url Dispatcher";

    @EJB
    private ConfigurationService configurationService;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        if (businessUniverseConfig.getBaseUrlConfig() != null && businessUniverseConfig.getBaseUrlConfig().getValue() != null) {
            ServletContext context = servletContextEvent.getServletContext();
            FilterRegistration filterRegistration = context.getFilterRegistration(LOGIN_FILTER);
            filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/"+businessUniverseConfig.getBaseUrlConfig().getValue() + URL_WILDCARD);
            ServletRegistration.Dynamic dynContext = context.addServlet(BASE_URL_SERVLET, BaseUrlDispatcherServlet.class);
            dynContext.setLoadOnStartup(1);
            dynContext.addMapping("/"+businessUniverseConfig.getBaseUrlConfig().getValue() + URL_WILDCARD);
        }


    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
