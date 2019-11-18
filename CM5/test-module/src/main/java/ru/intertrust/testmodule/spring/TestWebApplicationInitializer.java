package ru.intertrust.testmodule.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.intertrust.cm.core.initialize.PlatformWebApplicationInitializer;
import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

public class TestWebApplicationInitializer extends PlatformWebApplicationInitializer{

    @Override
    public void onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory) {

        //Make Sochi-Platform beans as parent spring-context
    	SingletonBeanFactoryLocator locator = SingletonBeanFactoryLocator.getInstance();
        ApplicationContext sochiPlatformContext = (ApplicationContext) locator.getBeanFactory("ear.context");

        // Create 'Sochi-Server' application context
        ClassPathXmlApplicationContext cmSochiContext = new ClassPathXmlApplicationContext();
        cmSochiContext.setConfigLocation("classpath*:bean-ex.xml");
        cmSochiContext.setParent(sochiPlatformContext);
        cmSochiContext.refresh();

    }
}
