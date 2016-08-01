package ru.intertrust.cm.test.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.testmodule.spring.TestSpringBeanInSecondContext;

@Plugin(name="TestPlugin2", description="Описание плагина 2", context="second-context-name")
public class TestPlugin2 implements PluginHandler{
    private static final Logger logger = LoggerFactory.getLogger(TestPlugin2.class);
    @Autowired
    private TestSpringBeanInSecondContext testSpringBeanInSecondContext;
    
    
    @Override
    public String execute(String param) {
        logger.info("Start plugin TestPlugin2");
        return "Плагин TestPlugin2 отработал; testSpringBeanInSecondContext=" + testSpringBeanInSecondContext  + " Параметр=" + param;
    }
}
