package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.TopLevelConfigurationCache;

import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

public class GlobalSettingsLogicalValidatorTest {
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    private static final String GLOBAL_SECOND_XML_PATH = "config/global-second-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateGlobalSettings() throws Exception {
        Configuration configuration =
                createConfiguration(GLOBAL_XML_PATH, DOMAIN_OBJECTS_CONFIG_PATH);

       GlobalSettingsLogicalValidator globalSettingsLogicalValidator =
                new GlobalSettingsLogicalValidator(configuration);
        globalSettingsLogicalValidator.validate();
    }

    @Test
    public void validateIncorrectCollectionView() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("There are more then one global settings configurations!");

        Configuration configuration = createConfiguration(GLOBAL_XML_PATH,
                GLOBAL_SECOND_XML_PATH, DOMAIN_OBJECTS_CONFIG_PATH);

        GlobalSettingsLogicalValidator globalSettingsLogicalValidator =
                new GlobalSettingsLogicalValidator(configuration);
        globalSettingsLogicalValidator.validate();

    }

    private Configuration createConfiguration(String... configPaths) throws Exception {
        TopLevelConfigurationCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configs = new HashSet<>();
        for(String path : configPaths) {
            configs.add(path);
        }
        configurationSerializer.setCoreConfigurationFilePaths(configs);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        return configuration;
    }

}

