package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.FatalBeanException;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.impl.ModuleServiceImpl;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH;

/**
 * Test for {@link ru.intertrust.cm.core.config.UniqueNameLogicalValidator}
 */
public class UniqueNameLogicalValidatorTest {

    private static final String COLLECTION_VIEW_XML_PATH = "config/collection-view-test.xml";
    private static final String COLLECTION_XML_PATH = "config/collections-test.xml";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    private static final String DOMAIN_OBJECTS_DUPLICATED_NAME_CONFIG_PATH = "config/domain-objects-duplicated-name-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        // Check for valid configuration
        createConfigurationExplorer(DOMAIN_OBJECTS_CONFIG_PATH);

        expectedException.expect(FatalBeanException.class);
        expectedException.expectMessage("Configuration of domain-object-type with name 'Duplicated_Name' was validated with errors.Count: 1 Content:\n" +
                "There are top level configurations with identical name\n");

        // Check for configuration with duplicated name
        createConfigurationExplorer(DOMAIN_OBJECTS_DUPLICATED_NAME_CONFIG_PATH);
    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(configPath));

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl result = new ConfigurationExplorerImpl(configuration);
        result.init();
        result.validate();
        return result;
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        ModuleServiceImpl result = new ModuleServiceImpl();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        conf.getConfigurationPaths().add(COLLECTION_VIEW_XML_PATH);
        conf.getConfigurationPaths().add(COLLECTION_XML_PATH);
        conf.getConfigurationPaths().add(GLOBAL_XML_PATH);
        conf.getConfigurationPaths().add(configPath);
        conf.getConfigurationPaths().add(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH);        
        conf.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);
        return result;
    }
}
