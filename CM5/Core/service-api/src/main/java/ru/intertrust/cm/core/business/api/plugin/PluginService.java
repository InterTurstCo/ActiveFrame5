package ru.intertrust.cm.core.business.api.plugin;

import org.springframework.context.ApplicationContext;

public interface PluginService {
    void init(String contextName, ApplicationContext applicationContext);
    String executePlugin(String id, String param);
}
