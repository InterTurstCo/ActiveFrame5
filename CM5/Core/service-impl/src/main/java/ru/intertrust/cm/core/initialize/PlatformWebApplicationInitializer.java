package ru.intertrust.cm.core.initialize;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.WebApplicationInitializer;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.intertrust.cm.core.business.impl.GloballyLockableInitializer;
import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

/**
 * Created by Vitaliy.Orlov on 04.05.2018.
 */
public abstract class PlatformWebApplicationInitializer implements WebApplicationInitializer {
    private Logger logger = LoggerFactory.getLogger(PlatformWebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        try {
            logger.info("Initialized ejb context");

            //Загрузка контекста из файла beanRefContex.xml.
            final BeanFactory beanFactory = SingletonBeanFactoryLocator.getInstance().getBeanFactory(null);

            logger.info("Initialized spring context");

            GloballyLockableInitializer globallyLockableInitializer = beanFactory.getBean(GloballyLockableInitializer.class);

            logger.info("Start global initialize");
            globallyLockableInitializer.start();

            logger.info("Start app initialize");

            onInitContext(servletContext, beanFactory);

            // Проверить не добавлен ли уже листенер в servletContext при вызове onInitContext
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if (webApplicationContext == null){
                servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "META-INF/applicationContext.xml");
                servletContext.addListener(new ContextLoaderListener(){
                    @Override
                    protected ApplicationContext loadParentContext(ServletContext servletContext) {
                        return (ApplicationContext)beanFactory;
                    }
                });
            }

            logger.info("Finish app initialize");
            globallyLockableInitializer.finish();

            logger.info("Initial data loaded : complete");
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }

    }

    public abstract void onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory);

}
