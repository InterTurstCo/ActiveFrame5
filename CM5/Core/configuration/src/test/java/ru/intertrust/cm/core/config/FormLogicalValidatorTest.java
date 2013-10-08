package ru.intertrust.cm.core.config;

import org.junit.Test;
import ru.intertrust.cm.core.config.model.base.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
public class FormLogicalValidatorTest {
    private static final String FORM_XML_PATH =
            "config/forms-test.xml";

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(FORM_XML_PATH);

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        return configurationExplorer;
    }

}

