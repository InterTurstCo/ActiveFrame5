package ru.intertrust.cm.webcontext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class CmPlatformWebInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        //Make Sochi-Platform beans as parent spring-context
        BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
        BeanFactoryReference sochiPlatformContextReference = locator.useBeanFactory("ear.context");
        ApplicationContext sochiPlatformContext = (ApplicationContext) sochiPlatformContextReference.getFactory();

        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.setParent(sochiPlatformContext);

//        container.addListener(new ContextLoaderListener(webContext));

        //Register main CMJ-Server servlet
        DispatcherServlet servlet = new DispatcherServlet(webContext);
        servlet.setDispatchOptionsRequest(true);

        ServletRegistration.Dynamic dispatcher = container.addServlet("cmj", servlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/*");

        SpringContext.setContext(webContext);
    }
}
