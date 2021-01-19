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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH;
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
        List<LogicalErrors> errors = collectionViewValidator.validate();
        assertEquals(0, errors.size());
    }

    @Test
    public void validateIncorrectCollectionView() throws Exception {
        String expectedMessage = "Configuration of collection-view with name 'employees_default_view' "
                + "was validated with errors.Count: 1 Content:\n"
                + "Couldn't find field 'updated_date' in sql query for collection with name 'Employees'\n"
                + "Configuration of collection-view with name "
                + "'countries_view' was validated with errors.Count: 1 Content:\n"
                + "Couldn't find collection with name 'Countries'\n";
        try {
            ConfigurationExplorer configurationExplorer = createConfigurationExplorer(INVALID_COLLECTION_VIEW_XML_PATH);
        } catch (FatalBeanException exception){
            String exM = exception.getCause().getMessage();
            assertEquals(exM, expectedMessage);
        }

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(configPath));

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl result = new ConfigurationExplorerImpl(configuration);
        result.init();
        return result;
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        ModuleServiceImpl result = new ModuleServiceImpl();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        conf.getConfigurationPaths().add(DOMAIN_OBJECTS_CONFIG_PATH);
        conf.getConfigurationPaths().add(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH);        
        conf.getConfigurationPaths().add(COLLECTION_XML_PATH);
        conf.getConfigurationPaths().add(GLOBAL_XML_PATH);
        conf.getConfigurationPaths().add(configPath);
        conf.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);
        return result;
    }
}

