package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import ru.intertrust.cm.core.config.model.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;
import static ru.intertrust.cm.core.config.Constants.MODULES_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.MODULES_CONFIG_SCHEMA_PATH;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 06.09.13
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class NavigationPanelLogicalValidatorTest {
    private static final String NAVIGATION_PANEL_XML_PATH =
            "config/navigation-panel.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);
        configurationExplorer.build();
    }

    @Test
    public void logicalValidate() throws Exception {
     Configuration configuration = deserializeConfiguration(NAVIGATION_PANEL_XML_PATH);
        File file = new File (NAVIGATION_PANEL_XML_PATH);
        System.out.println(file.getAbsolutePath()) ;
        System.out.println(file.exists()) ;
     System.out.println(ConfigurationSerializer.serializeConfiguration(configuration));
    }

  /*  @Test
    public void testValidateInvalidExtendsAttribute() throws Exception {
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Navigation Panel Configuration was not found");

        configurationExplorer.build();
    }           */

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
        System.out.println(ConfigurationSerializer.serializeConfiguration(configuration));

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.build();

        return configurationExplorer;
    }
    private static Serializer createSerializerInstance() {
        Strategy strategy = new AnnotationStrategy();
        return new Persister(strategy);
    }
    private Configuration deserializeConfiguration(String configurationFilePath) throws Exception {

        return createSerializerInstance().read(Configuration.class, FileUtils.getFileInputStream(configurationFilePath));
    }
}

