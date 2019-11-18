package ru.intertrust.cm.core.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

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
            parentContext = (ApplicationContext) SingletonBeanFactoryLocator.getInstance().getBeanFactory(parentContextName);
        }
        
        return super.createWebApplicationContext(parentContext);
    }

}
