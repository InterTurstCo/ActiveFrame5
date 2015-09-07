package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;

import javax.ejb.EJB;
import javax.servlet.*;
import java.util.EnumSet;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 07.09.2015
 */
public class CustomContextListener implements ServletContextListener {

    private static final String LOGIN_FILTER = "LoginFilter";
    private static final String URL_WILDCARD = "/*";
    private static Logger log = LoggerFactory.getLogger(CustomContextListener.class);

    @EJB
    private ConfigurationService configurationService;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.info("Custom listener INIT");
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        if(businessUniverseConfig.getBaseUrlConfig()!=null && businessUniverseConfig.getBaseUrlConfig().getValue()!=null){
            ServletContext context = servletContextEvent.getServletContext();
            FilterRegistration filterRegistration = context.getFilterRegistration(LOGIN_FILTER);
            filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),true,businessUniverseConfig.getBaseUrlConfig().getValue()+URL_WILDCARD);
        }


    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
