package ru.intertrust.cm.core.initialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.WebApplicationInitializer;
import ru.intertrust.cm.core.business.impl.GloballyLockableInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
            final BeanFactory beanFactory = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null).getFactory();

            logger.info("Initialized spring context");

            GloballyLockableInitializer globallyLockableInitializer = beanFactory.getBean(GloballyLockableInitializer.class);

            logger.info("Start global initialize");
            globallyLockableInitializer.start();

            logger.info("Start app initialize");

            onInitContext(servletContext, beanFactory);

            logger.info("Finish app initialize");
            globallyLockableInitializer.finish();

            logger.info("Initial data loaded : complete");
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }

    }

    public abstract void onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory);

}
