package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.impl.ModuleServiceImpl;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_CONFIG_PATH;

public class GlobalSettingsLogicalValidatorTest {
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    private static final String GLOBAL_SECOND_XML_PATH = "config/global-second-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateGlobalSettings() throws Exception {
        Configuration configuration =
                createConfiguration(GLOBAL_XML_PATH, DOMAIN_OBJECTS_CONFIG_PATH);

        GlobalSettingsLogicalValidator globalSettingsLogicalValidator = new GlobalSettingsLogicalValidator(configuration);
        List<LogicalErrors> errors = globalSettingsLogicalValidator.validate();
        assertEquals(0, errors.size());
    }

    @Test
    public void validateIncorrectCollectionView() throws Exception {
        String expectedException = "Configuration of global-settings with name 'Default' was validated with errors.Count: 1 Content:\n" +
                "There are more then one global settings configurations!\n";

        Configuration configuration = createConfiguration(GLOBAL_XML_PATH,
                GLOBAL_SECOND_XML_PATH, DOMAIN_OBJECTS_CONFIG_PATH);

        List<LogicalErrors> logicalErrorsList = new GlobalSettingsLogicalValidator(configuration).validate();
        assertEquals(expectedException, LogicalErrors.toString(logicalErrorsList));
    }

    private Configuration createConfiguration(String... configPaths) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configs = new HashSet<>();
        for (String path : configPaths) {
            configs.add(path);
        }
        configurationSerializer.setModuleService(createModuleService(configs));

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        return configuration;
    }

    private ModuleService createModuleService(Set<String> configs) throws MalformedURLException {
        ModuleServiceImpl result = new ModuleServiceImpl();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        for (String config : configs) {
            conf.getConfigurationPaths().add(config);
        }
        conf.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        final URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);

        return result;
    }
}

