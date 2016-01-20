package ru.intertrust.cm.core.web;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.context.WebApplicationContext;

/**
 * Реализация спрингового DispatcherServlet, для возможности явно задать родительский контекст для контекста сервлета
 * @author larin
 *
 */
public class DispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {
    private static final long serialVersionUID = -4947317206814747008L;
    public static final String PARENT_CONTEXT = "PARENT_CONTEXT";
    
    
    @Override
    protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
        String parentContextName = getServletConfig().getInitParameter(PARENT_CONTEXT);
        
        ApplicationContext parentContext = parent;
        //Подмена родительского контекста
        if (parentContextName != null){
            BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
            BeanFactoryReference sochiPlatformContextReference = locator.useBeanFactory(parentContextName);
            parentContext = (ApplicationContext)sochiPlatformContextReference.getFactory();
        }
        
        return super.createWebApplicationContext(parentContext);
    }

}
