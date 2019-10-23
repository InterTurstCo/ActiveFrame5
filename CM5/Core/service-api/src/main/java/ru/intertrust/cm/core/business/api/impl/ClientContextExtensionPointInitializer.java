package ru.intertrust.cm.core.business.api.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.config.server.ServerStatus;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;

/**
 * Класс загрузчик точек расширения в дочернем спринговом контексте относительно ядерного
 * @author larin
 *
 */
public class ClientContextExtensionPointInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ClientContextExtensionPointInitializer.class);

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private PluginService pluginService;
    
    @Autowired
    private ApplicationContext context;

    private String contextName;

    private List<String> packages;

    /**
     * Загрузка точек расширения переданного контекста в определенных пакетах
     * @param contextName
     * @param packages
     * @deprecated  При загрузке используются пакеты из cm-module, переданные здесь пакеты игнорируются {@link #ClientContextExtensionPointInitializer(String)}
     */
    @Deprecated()
    public ClientContextExtensionPointInitializer(String contextName, List<String> packages) {
        this.contextName = contextName;
        this.packages = packages;
    }

    /**
     * Загрузка точек расширения переданного контекста в определенных пакетах
     * @param contextName
     */
    public ClientContextExtensionPointInitializer(String contextName) {
        this.contextName = contextName;
    }

    @PostConstruct
    public void init() {
        logger.info("start init");
        if (ServerStatus.isEnable()) {
            extensionService.init(contextName, context);
            pluginService.init(contextName, context);
            logger.info("end init");
        }else {
            logger.info("Af5 is disabled");
        }
    }
}
