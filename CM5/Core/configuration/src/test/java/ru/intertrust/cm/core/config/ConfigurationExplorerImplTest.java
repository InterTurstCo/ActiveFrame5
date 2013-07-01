package ru.intertrust.cm.core.config;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 3:43 PM
 */
public class ConfigurationExplorerImplTest {

    private static final String PERSON_CONFIG_NAME = "Person";
    private static final String EMPLOYEES_CONFIG_NAME = "Employees";
    private static final String E_MAIL_CONFIG_NAME = "EMail";

    private static final String CONFIGURATION_SCHEMA_PATH = "test-config/configuration-test.xsd";
    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects-test.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections-test.xml";

    private static final Set<String> CONFIG_PATHS =
            new HashSet<>(Arrays.asList(DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH));

    private Configuration configuration;
    private ConfigurationExplorerImpl configurationExplorer;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePaths(CONFIG_PATHS);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configuration = configurationSerializer.serializeConfiguration();
        configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.build();
    }

    @Test
    public void testGetConfiguration() throws Exception {
        Configuration testConfiguration = configurationExplorer.getConfiguration();
        assertTrue(configuration == testConfiguration);
    }

    @Test
    public void testSetConfiguration() throws Exception {
        configurationExplorer.setConfiguration(configuration);
        Configuration testConfiguration = configurationExplorer.getConfiguration();
        assertTrue(configuration == testConfiguration);
    }

    @Test
    public void testInit() throws Exception {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getDomainObjectConfig(PERSON_CONFIG_NAME);
        assertNotNull(domainObjectTypeConfig);

        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(PERSON_CONFIG_NAME, E_MAIL_CONFIG_NAME);
        assertNotNull(fieldConfig);

        CollectionConfig collectionConfig = configurationExplorer.getCollectionConfig(EMPLOYEES_CONFIG_NAME);
        assertNotNull(collectionConfig);
    }

    @Test
    public void testGetDomainObjectConfigs() throws Exception {
        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs = configurationExplorer.getDomainObjectConfigs();

        assertNotNull(domainObjectTypeConfigs);
        assertEquals(domainObjectTypeConfigs.size(), 4);

        List<String> domainObjectNames = new ArrayList<>();
        domainObjectNames.addAll(Arrays.asList("Outgoing Document", PERSON_CONFIG_NAME, "Employee", "Department"));

        for(DomainObjectTypeConfig domainObjectTypeConfig : domainObjectTypeConfigs) {
            String name = domainObjectTypeConfig.getName();
            assertTrue(domainObjectNames.contains(name));
            domainObjectNames.remove(name);
        }
    }

    @Test
    public void testGetCollectionConfigs() throws Exception {
        Collection<CollectionConfig> collectionConfigs = configurationExplorer.getCollectionConfigs();

        assertNotNull(collectionConfigs);
        assertEquals(collectionConfigs.size(), 2);

        List<String> collectionNames = new ArrayList<>();
        collectionNames.addAll(Arrays.asList(EMPLOYEES_CONFIG_NAME, "Employees_2"));

        for(CollectionConfig collectionConfig : collectionConfigs) {
            String name = collectionConfig.getName();
            assertTrue(collectionNames.contains(name));
            collectionNames.remove(name);
        }
    }

    @Test
    public void testGetDomainObjectConfig() throws Exception {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getDomainObjectConfig(PERSON_CONFIG_NAME);
        assertNotNull(domainObjectTypeConfig);
        assertEquals(domainObjectTypeConfig.getName(), PERSON_CONFIG_NAME);
    }

    @Test
    public void testGetCollectionConfig() throws Exception {
        CollectionConfig collectionConfig = configurationExplorer.getCollectionConfig(EMPLOYEES_CONFIG_NAME);
        assertNotNull(collectionConfig);
        assertEquals(collectionConfig.getName(), EMPLOYEES_CONFIG_NAME);
    }

    @Test
    public void testGetFieldConfig() throws Exception {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(PERSON_CONFIG_NAME, E_MAIL_CONFIG_NAME);
        assertNotNull(fieldConfig);
        assertEquals(fieldConfig.getName(), E_MAIL_CONFIG_NAME);
    }
}
