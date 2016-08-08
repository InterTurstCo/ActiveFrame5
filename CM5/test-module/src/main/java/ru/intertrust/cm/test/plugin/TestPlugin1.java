package ru.intertrust.cm.test.plugin;

import javax.ejb.EJBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;

@Plugin(name="TestPlugin1", description="Описание плагина 1")
public class TestPlugin1 implements PluginHandler{
    private static final Logger logger = LoggerFactory.getLogger(TestPlugin1.class);
    @Autowired
    private CollectionsService collectionsService;
    
    
    @Override
    public String execute(EJBContext context, String param) {
        logger.info("Start plugin TestPlugin1");
        return "Плагин TestPlugin1 отработал; collectionsService=" + collectionsService  + " Параметр=" + param;
    }
}
