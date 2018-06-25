package ru.intertrust.testmodule.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.WebApplicationInitializer;
import ru.intertrust.cm.core.initialize.PlatformWebApplicationInitializer;

public class TestWebApplicationInitializer extends PlatformWebApplicationInitializer{

    @Override
    public void onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory) {

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
