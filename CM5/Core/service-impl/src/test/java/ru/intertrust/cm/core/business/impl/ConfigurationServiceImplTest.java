package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.CollectionsConfiguration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.DomainObjectsConfiguration;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author vmatsukevich
 *         Date: 5/27/13
 *         Time: 5:25 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

    private static final String BUSINESS_OBJECTS_CONFIG_PATH = "test-config/business-objects.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections.xml";
    private static final String CONFIGURATION_SCHEMA = "test-config/configuration.xsd";

    @InjectMocks
    private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl();
    @Mock
    private DataStructureDAO dataStructureDAOMock;
    @Mock
    private AuthenticationService authenticationService;

    private DomainObjectsConfiguration domainObjectsConfiguration;
    private CollectionsConfiguration collectionsConfiguration;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePath(BUSINESS_OBJECTS_CONFIG_PATH);
        configurationSerializer.setCollectionsConfigurationFilePath(COLLECTIONS_CONFIG_PATH);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA);

        domainObjectsConfiguration = configurationSerializer.serializeBusinessObjectConfiguration();
        assertNotNull(domainObjectsConfiguration); // проверяем, что конфигурация сериализована из файла

        collectionsConfiguration = configurationSerializer.serializeCollectionConfiguration();
        assertNotNull(collectionsConfiguration); // проверяем, что конфигурация сериализована из файла

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorer();
        configurationExplorer.setDomainObjectsConfiguration(domainObjectsConfiguration);
        configurationExplorer.setCollectionsConfiguration(collectionsConfiguration);
        configurationExplorer.init();

        configurationService.setConfigurationExplorer(configurationExplorer);
    }

    @Test
    public void testLoadConfigurationWhenLoaded() throws Exception {
        when(dataStructureDAOMock.countTables()).thenReturn(10);
        configurationService.loadConfiguration();

        verify(dataStructureDAOMock).countTables();
        verify(dataStructureDAOMock, never()).createServiceTables();
        verify(dataStructureDAOMock, never()).createTable(Matchers.<DomainObjectConfig>anyObject());
    }

    @Test
    public void testLoadConfigurationWhenNotLoaded() throws Exception {
        when(dataStructureDAOMock.countTables()).thenReturn(0);
        configurationService.loadConfiguration();

        verify(dataStructureDAOMock).countTables();
        verify(dataStructureDAOMock).createServiceTables();
        verify(dataStructureDAOMock, times(domainObjectsConfiguration.getDomainObjectConfigs().size())).createTable(Matchers.<DomainObjectConfig>anyObject());
    }
}
