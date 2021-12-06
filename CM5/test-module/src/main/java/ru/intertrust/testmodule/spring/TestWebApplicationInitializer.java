package ru.intertrust.testmodule.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.intertrust.cm.core.initialize.PlatformWebApplicationInitializer;
import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

import javax.servlet.ServletContext;

public class TestWebApplicationInitializer extends PlatformWebApplicationInitializer{

    @Override
    public ApplicationContext onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory) {

        //Make Sochi-Platform beans as parent spring-context
        ApplicationContext sochiPlatformContext = (ApplicationContext) platformBeanFactory;

        // Create 'Sochi-Server' application context
        ClassPathXmlApplicationContext cmSochiContext = new ClassPathXmlApplicationContext();
        cmSochiContext.setConfigLocation("classpath*:bean-ex.xml");
        cmSochiContext.setParent(sochiPlatformContext);
        cmSochiContext.refresh();

        return cmSochiContext;
    }
}
