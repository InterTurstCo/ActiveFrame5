package ru.intertrust.cm.core.config.localization;

import ru.intertrust.cm.core.config.module.LocalizationFileConfiguration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.io.*;
import java.net.MalformedURLException;
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

    @org.springframework.beans.factory.annotation.Value("${property.files.substitution.enabled:false}")
    private boolean propertyFilesSubstitutionEnabled;

    @org.springframework.beans.factory.annotation.Value("${localization.folder:#{null}}")
    private String localizationFolderPath;

    private ModuleService moduleService;

    public LocalizationLoaderImpl(ModuleService moduleService) {
        this.moduleService = moduleService;
        MessageResourceProvider.setLocaleToResource(load());
    }

    private Map<String, Map<String, String>> propertiesByLocale = new HashMap<>();

    @Override
    public Map<String, Map<String, String>> load() {
        Map<String, Map<String, Map>> overriddenLocalizationFiles = getOverriddenLocalizationProperties();

        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getLocalisationFiles() != null) {
                URL moduleUrl = moduleConfiguration.getModuleUrl();
                for (LocalizationFileConfiguration config : moduleConfiguration.getLocalisationFiles().getLocalizationFiles()) {
                    String propertiesPath = moduleUrl.toString() + config.getFilePath();
                    Map moduleProps = loadProperties(propertiesPath);

                    if (overriddenLocalizationFiles != null) {
                        Map<String, Map> moduleOverriddenFiles = overriddenLocalizationFiles.get(moduleConfiguration.getName());
                        if (moduleOverriddenFiles != null) {
                            File localizationFile = new File(propertiesPath);
                            Map overriddenProperties = moduleOverriddenFiles.get(localizationFile.getName());
                            if (overriddenProperties != null) {
                                moduleProps.putAll(overriddenProperties);
                            }
                        }
                    }

                    String locale = config.getLocale();
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

    private Map<String, Map<String, Map>> getOverriddenLocalizationProperties() {
        if (!propertyFilesSubstitutionEnabled) {
            return null;
        }

        Map<String, Map<String, Map>> overriddenLocalizationFiles = new HashMap<>();

        if (localizationFolderPath == null) {
            return overriddenLocalizationFiles;
        }

        File localizationFolder = new File(localizationFolderPath);

        if (!localizationFolder.exists() || !localizationFolder.isDirectory()) {
            return overriddenLocalizationFiles;
        }

        File[] moduleLocalizationFolders = localizationFolder.listFiles();

        if (moduleLocalizationFolders == null || moduleLocalizationFolders.length == 0) {
            return overriddenLocalizationFiles;
        }

        for (File moduleLocalizationFolder : moduleLocalizationFolders) {
            if (!moduleLocalizationFolder.exists() || !moduleLocalizationFolder.isDirectory()) {
                continue;
            }

            Map<String, Map> moduleProperties = overriddenLocalizationFiles.get(moduleLocalizationFolder.getName());
            if (moduleProperties == null) {
                moduleProperties = new HashMap<>();
                overriddenLocalizationFiles.put(moduleLocalizationFolder.getName(), moduleProperties);
            }

            File[] moduleLocalizationFiles = moduleLocalizationFolder.listFiles();
            if (moduleLocalizationFiles != null && moduleLocalizationFiles.length > 0) {
                for (File localizationFile : moduleLocalizationFiles) {
                    if (localizationFile.exists() && localizationFile.isFile()) {
                        try {
                            Map properties = loadProperties(localizationFile.toURI().toURL().toString());
                            moduleProperties.put(localizationFile.getName(), properties);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return overriddenLocalizationFiles;
    }

    private Map loadProperties(String path) {
        Properties moduleProps = new Properties();
        try {
            URL fileUrl = new URL(path);
            try(InputStream inputStream = fileUrl.openStream()) {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                moduleProps.load(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moduleProps;
    }

}
