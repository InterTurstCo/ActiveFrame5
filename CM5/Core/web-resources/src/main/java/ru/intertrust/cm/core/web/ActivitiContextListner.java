package ru.intertrust.cm.core.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngine;

import ru.intertrust.cm.core.util.SpringApplicationContext;

public class ActivitiContextListner implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ProcessEngine processEngine = SpringApplicationContext.getContext().getBean(ProcessEngine.class);
        processEngine.close();        
    }

}
