package ru.intertrust.cm.test.plugin;

import javax.ejb.EJBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;

@Plugin(name="TestPlugin3", description="Описание плагина 3", autostart=true)
public class TestPlugin3 implements PluginHandler{
    private static final Logger logger = LoggerFactory.getLogger(TestPlugin3.class);
    
    @Autowired
    private CollectionsService collectionsService;
    
    @Override
    public String execute(EJBContext context, String param) {
        logger.info("Start plugin TestPlugin3");
        return "Плагин TestPlugin3 отработал; collectionsService=" + collectionsService + " Параметр=" + param;
    }
}
