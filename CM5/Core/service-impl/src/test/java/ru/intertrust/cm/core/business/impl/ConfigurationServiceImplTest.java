package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;

import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    private DataStructureDao dataStructureDao;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private AuthenticationService authenticationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ConfigurationExplorerImpl configurationExplorer;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setConfigurationFilePaths(CONFIG_PATHS);
        configurationSerializer.setConfigurationSchemaFilePath(CONFIGURATION_SCHEMA);

        configuration = configurationSerializer.serializeConfiguration();
        assertNotNull(configuration); // проверяем, что конфигурация сериализована из файла

        configurationExplorer = new ConfigurationExplorerImpl();
        configurationExplorer.setConfiguration(configuration);
        configurationExplorer.build();

        configurationService.setConfigurationExplorer(configurationExplorer);
    }

    @Test
    public void testLoadConfigurationLoadedButNotSaved() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(10);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(null);

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration loading aborted: configuration was previously " +
                "loaded but wasn't saved");

        configurationService.loadConfiguration();
    }

    @Test
    public void testLoadConfigurationUpdated() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(10);

        String configurationString = ConfigurationSerializer.deserializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);

        // Вносим изменения в конфигурацию
        DomainObjectConfig domainObjectConfig = configurationExplorer.getDomainObjectConfig("Outgoing Document");

        StringFieldConfig descriptionFieldConfig = new StringFieldConfig();
        descriptionFieldConfig.setName("Long Description");
        descriptionFieldConfig.setLength(256);
        descriptionFieldConfig.setNotNull(false);
        domainObjectConfig.getFieldConfigs().add(descriptionFieldConfig);

        ReferenceFieldConfig executorFieldConfig = new ReferenceFieldConfig();
        executorFieldConfig.setName("Executor");
        executorFieldConfig.setType("Employee");
        executorFieldConfig.setNotNull(true);
        domainObjectConfig.getFieldConfigs().add(executorFieldConfig);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("Registration Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);
        domainObjectConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        // Пересобираем configurationExplorer
        configurationExplorer.build();

        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        verify(dataStructureDao).updateTableStructure(anyString(), anyListOf(FieldConfig.class),
                anyListOf(UniqueKeyConfig.class));
        verify(configurationDao).save(ConfigurationSerializer.deserializeConfiguration(configuration));
    }

    @Test
    public void testLoadConfigurationNoUpdate() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(10);

        String configurationString = ConfigurationSerializer.deserializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);

        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        verify(dataStructureDao, never()).createServiceTables();
        verify(dataStructureDao, never()).createTable(any(DomainObjectConfig.class));
        verify(dataStructureDao, never()).createSequence(any(DomainObjectConfig.class));
        verify(configurationDao, never()).save(anyString());
    }

    @Test
    public void testLoadConfigurationFirstTime() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(0);
        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        verify(dataStructureDao).createServiceTables();
        verify(dataStructureDao, times(4)).createTable(any(DomainObjectConfig.class));
        verify(dataStructureDao, times(4)).createSequence(any(DomainObjectConfig.class));
        verify(configurationDao).save(ConfigurationSerializer.deserializeConfiguration(configuration));
    }
}
