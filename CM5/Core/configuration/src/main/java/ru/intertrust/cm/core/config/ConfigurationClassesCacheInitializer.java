package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Служит для инициализации {@link ru.intertrust.cm.core.config.converter.ConfigurationClassesCache} с помощью спринг
 * @author vmatsukevich
 *         Date: 7/15/13
 *         Time: 1:22 PM
 */
public class ConfigurationClassesCacheInitializer {

    @Autowired private ModuleService moduleService;

    /**
     * Создает {@link ConfigurationClassesCacheInitializer}
     */
    public ConfigurationClassesCacheInitializer() {
    }

    /**
     * Инициализирует {@link ConfigurationClassesCacheInitializer}
     * списком пакетов из тегов configuration-elements-packages файлов cm-module.xml
     */
    public void init() {
        ConfigurationClassesCache cache = ConfigurationClassesCache.getInstance();
        ArrayList<String> packages = new ArrayList<>();
        for (ModuleConfiguration config : moduleService.getModuleList()) {
            List<String> modulePackages = config.getConfigurationElementsPackages();
            if (modulePackages != null) {
                packages.addAll(modulePackages);
            }
        }
        if (packages.size() > 0) {
            cache.setSearchClassPackages(packages);
        }
        cache.build();
    }
}
