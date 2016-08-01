package ru.intertrust.cm.core.business.api.plugin;

import java.util.Map;

import org.springframework.context.ApplicationContext;

public interface PluginService {
    void init(String contextName, ApplicationContext applicationContext);
    Map<String, PluginInfo> getPlugins();
    String executePlugin(String id, String param);
    void deployPluginPackage(String filePath);
}
