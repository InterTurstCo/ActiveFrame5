package ru.intertrust.cm.core.config.localization;

import ru.intertrust.cm.core.config.module.LocalizationFileConfiguration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 18:05
 */


public class LocalizationLoaderImpl implements LocalizationLoader, LocalizationLoader.Remote {

    private ModuleService moduleService;

    public LocalizationLoaderImpl(ModuleService moduleService) {
        this.moduleService = moduleService;
        MessageResourceProvider.setLocaleToResource(load());
    }

    private Map<String, Map<String, String>> propertiesByLocale = new HashMap<>();

    @Override
    public Map<String, Map<String, String>> load() {
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getLocalisationFiles() != null) {
                URL moduleUrl = moduleConfiguration.getModuleUrl();
                for (LocalizationFileConfiguration config : moduleConfiguration.getLocalisationFiles().getLocalizationFiles()) {
                    Map moduleProps = loadProperties(moduleUrl.toString() + config.getFilePath());
                    String locale = config.getLocale() != null ? config.getLocale() : MessageResourceProvider.DEFAULT_LOCALE;
                    Map<String, String> properties = propertiesByLocale.get(locale);
                    if (properties == null) {
                        properties = new HashMap<>();
                        propertiesByLocale.put(locale, properties);
                    }
                    properties.putAll(moduleProps);
                }
            }
        }
        return propertiesByLocale;
    }

    private Map loadProperties(String path) {
        Properties moduleProps = new Properties();
        try {
            URL fileUrl = new URL(path);
            InputStream inputStream = fileUrl.openStream();
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            moduleProps.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moduleProps;
    }

}
