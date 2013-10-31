package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
public class CollectionViewLogicalValidatorTest {
    private static final String COLLECTION_VIEW_XML_PATH = "config/collection-view-test.xml";
    private static final String INVALID_COLLECTION_VIEW_XML_PATH = "config/collection-view-with-errors.xml";
    private static final String COLLECTION_XML_PATH = "config/collections-test.xml";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateCorrectCollectionView() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(COLLECTION_VIEW_XML_PATH);

        CollectionViewLogicalValidator collectionViewValidator =
                new CollectionViewLogicalValidator(configurationExplorer);
        collectionViewValidator.validate();
    }

    @Test
    public void validateIncorrectCollectionView() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of collection-view with name "
                + "'countries_view' was validated with errors.Count: 1 Content:\n"
                + "Couldn't find collection with name 'Countries'\n"
                + "Configuration of collection-view with name 'employees_default_view' "
                + "was validated with errors.Count: 1 Content:\n"
                + "Couldn't find field 'updated_date' in sql query for collection with name 'Employees'");

        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(INVALID_COLLECTION_VIEW_XML_PATH);

        CollectionViewLogicalValidator collectionViewValidator =
                new CollectionViewLogicalValidator(configurationExplorer);
        collectionViewValidator.validate();

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.
                asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH, COLLECTION_XML_PATH, GLOBAL_XML_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl(configuration);

        configurationExplorer.build();
        return configurationExplorer;
    }

}

