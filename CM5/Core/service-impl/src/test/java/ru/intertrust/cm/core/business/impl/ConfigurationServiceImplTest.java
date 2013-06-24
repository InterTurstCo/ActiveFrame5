package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;

/**
 * @author vmatsukevich
 *         Date: 5/27/13
 *         Time: 5:25 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

    private static final String DOMAIN_OBJECTS_CONFIG_PATH = "test-config/domain-objects.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections.xml";
    private static final String CONFIGURATION_SCHEMA = "test-config/configuration.xsd";
    private static final Set<String> CONFIG_PATHS = new HashSet<>(Arrays.asList(
            new String[]{DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH}));

    @InjectMocks
    private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl();
    @Mock
    private DataStructureDAO dataStructureDAOMock;
    @Mock
    private AuthenticationService authenticationService;

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePaths(CONFIG_PATHS);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA);

        configuration = configurationSerializer.serializeConfiguration();
        assertNotNull(configuration); // проверяем, что конфигурация сериализована из файла

        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl();
        configurationExplorer.setConfiguration(configuration);
        configurationExplorer.init();

        configurationService.setConfigurationExplorer(configurationExplorer);
    }

    @Test
    public void testLoadConfigurationWhenLoaded() throws Exception {
//        when(dataStructureDAOMock.countTables()).thenReturn(10);
//        configurationService.loadConfiguration();
//
//        verify(dataStructureDAOMock).countTables();
//        verify(dataStructureDAOMock, never()).createServiceTables();
//        verify(dataStructureDAOMock, never()).createTable(Matchers.<DomainObjectConfig>anyObject());
    }

    @Test
    public void testLoadConfigurationWhenNotLoaded() throws Exception {
//        when(dataStructureDAOMock.countTables()).thenReturn(0);
//        configurationService.loadConfiguration();
//
//        verify(dataStructureDAOMock).countTables();
//        verify(dataStructureDAOMock).createServiceTables();
//        verify(dataStructureDAOMock, times(4)).createTable(Matchers.<DomainObjectConfig>anyObject());
    }
}
