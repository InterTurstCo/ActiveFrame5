package ru.intertrust.cm.core.business.api.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.dao.api.ExtensionService;

/**
 * Класс загрузчик точек расширения в дочернем спринговом контексте относительно ядерного
 * @author larin
 *
 */
public class ClientContextExtensionPointInitializer {
    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private PluginService pluginService;
    
    @Autowired
    private ApplicationContext context;

    private String contextName;

    private List<String> packages;

    public ClientContextExtensionPointInitializer(String contextName, List<String> packages) {
        this.contextName = contextName;
        this.packages = packages;
    }
    
    @PostConstruct
    public void init() {
        extensionService.init(contextName, context, packages);
        pluginService.init(contextName, context);
    }
}
