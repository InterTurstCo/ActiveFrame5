package ru.intertrust.cm.core.business.api.plugin;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

public interface PluginStorage {

    Map<String, PluginInfo> getPlugins();

    void deployPluginPackage(String filePath);
    
    void init(String contextName, ApplicationContext applicationContext);

    PluginHandler getPluginHandler(PluginInfo pluginInfo);

    DomainObject getPluginStatus(String pluginId);
}
