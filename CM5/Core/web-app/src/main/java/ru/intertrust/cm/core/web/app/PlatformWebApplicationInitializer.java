package ru.intertrust.cm.core.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Vitaliy.Orlov on 04.05.2018.
 */
public class PlatformWebApplicationInitializer implements WebApplicationInitializer {
    private Logger logger = LoggerFactory.getLogger(PlatformWebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        try {
            logger.info("Initialized ejb context");

            //Загрузка контекста из файла beanRefContex.xml.
            final BeanFactory beanFactory = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null).getFactory();

            logger.info("Initialized spring context");

            executeBeanMethod(beanFactory, "globallyLockableInitializer", "start");

            executeBeanMethod(beanFactory, "globallyLockableInitializer", "finish");

            logger.info("Initial data loaded");
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }

    }


    private void executeBeanMethod(BeanFactory factory, String beanName, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Object scheduleTaskLoader = factory.getBean(beanName);
        scheduleTaskLoader.getClass().getMethod(methodName).invoke(scheduleTaskLoader);
    }
}
