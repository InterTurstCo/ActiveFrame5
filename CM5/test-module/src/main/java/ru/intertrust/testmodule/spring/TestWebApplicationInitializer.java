package ru.intertrust.testmodule.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.WebApplicationInitializer;

public class TestWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {

        //Make Sochi-Platform beans as parent spring-context
        BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
        BeanFactoryReference sochiPlatformContextReference = locator.useBeanFactory("ear.context");
        ApplicationContext sochiPlatformContext = (ApplicationContext) sochiPlatformContextReference.getFactory();

        // Create 'Sochi-Server' application context
        ClassPathXmlApplicationContext cmSochiContext = new ClassPathXmlApplicationContext();
        cmSochiContext.setConfigLocation("classpath*:bean-ex.xml");
        cmSochiContext.setParent(sochiPlatformContext);
        cmSochiContext.refresh();

    }

}
