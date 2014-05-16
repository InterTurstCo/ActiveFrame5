package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
/**
 * @author vmatsukevich
 *         Date: 5/27/13
 *         Time: 5:25 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLoadServiceImplTest {

    @InjectMocks
    private ConfigurationLoadServiceImpl configurationService = new ConfigurationLoadServiceImpl();
    @Mock
    private DataStructureDao dataStructureDao;
    @Mock
    private DomainObjectTypeIdDao domainObjectTypeIdDao;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private ConfigurationSerializer configurationSerializer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ApplicationContext context;

    private ConfigurationExplorerImpl configExplorer;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        ConfigurationClassesCache.getInstance().build();

        configuration = createConfiguration();
        configurationService.setConfigurationExplorer(new ConfigurationExplorerImpl(configuration));
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

        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);
        when(configurationSerializer.deserializeTrustedConfiguration(configurationString, false)).thenReturn(configuration);

        Configuration updatedConfiguration = createConfiguration();
        configExplorer = new ConfigurationExplorerImpl(updatedConfiguration);

        // Вносим изменения в конфигурацию
        DomainObjectTypeConfig domainObjectTypeConfig =
                configExplorer.getConfig(DomainObjectTypeConfig.class, "Outgoing_Document");

        StringFieldConfig descriptionFieldConfig = new StringFieldConfig();
        descriptionFieldConfig.setName("Long_Description");
        descriptionFieldConfig.setLength(256);
        descriptionFieldConfig.setNotNull(false);
        domainObjectTypeConfig.getFieldConfigs().add(descriptionFieldConfig);

        ReferenceFieldConfig executorFieldConfig = new ReferenceFieldConfig();
        executorFieldConfig.setName("Executor");
        executorFieldConfig.setType("Employee");
        executorFieldConfig.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(executorFieldConfig);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("Registration_Number");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);
        domainObjectTypeConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        // Пересобираем configExplorer
        configExplorer = new ConfigurationExplorerImpl(updatedConfiguration);
        configurationService.setConfigurationExplorer(configExplorer);

        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        //verify(dataStructureDao).updateTableStructure(anyString(), anyListOf(FieldConfig.class));
//        verify(configurationDao).save(ConfigurationSerializer.serializeConfiguration(updatedConfiguration));
    }

    @Test
    public void testLoadConfigurationNoUpdate() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(10);

        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);
        when(configurationSerializer.deserializeTrustedConfiguration(configurationString, false)).thenReturn(configuration);

        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        verify(dataStructureDao, never()).createServiceTables();
        verify(dataStructureDao, never()).createTable(any(DomainObjectTypeConfig.class));
        verify(dataStructureDao, never()).createSequence(any(DomainObjectTypeConfig.class));
        verify(configurationDao, never()).save(anyString());
    }

    @Test
    public void testLoadConfigurationFirstTime() throws Exception {
        when(dataStructureDao.countTables()).thenReturn(0);
        configurationService.loadConfiguration();

        verify(dataStructureDao).countTables();
        verify(dataStructureDao).createServiceTables();
        verify(dataStructureDao, times(2)).createTable(any(DomainObjectTypeConfig.class));
        verify(dataStructureDao, times(2)).createSequence(any(DomainObjectTypeConfig.class));
        verify(configurationDao).save(ConfigurationSerializer.serializeConfiguration(configuration));
    }

    private DomainObjectTypeConfig createOutgoingDocument() {
        DomainObjectTypeConfig result = new DomainObjectTypeConfig();
        result.setName("Outgoing_Document");

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("Registration_Number");
        registrationNumber.setLength(256);
        registrationNumber.setNotNull(true);
        result.getFieldConfigs().add(registrationNumber);

        DateTimeFieldConfig registrationDate = new DateTimeFieldConfig();
        registrationDate.setName("Registration_Date");
        registrationDate.setNotNull(true);
        result.getFieldConfigs().add(registrationDate);

        return result;
    }

    private DomainObjectTypeConfig createEmployee() {
        DomainObjectTypeConfig result = new DomainObjectTypeConfig();
        result.setName("Employee");

        return result;
    }

    private Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        GlobalSettingsConfig globalSettings = new GlobalSettingsConfig();
        AuditLog auditLog = new AuditLog();
        globalSettings.setAuditLog(auditLog);

        SqlTrace sqlTrace = new SqlTrace();
        sqlTrace.setEnable(true);
        sqlTrace.setResolveParameters(true);
        globalSettings.setSqlTrace(sqlTrace);

        configuration.getConfigurationList().add(createOutgoingDocument());
        configuration.getConfigurationList().add(createEmployee());
        configuration.getConfigurationList().add(globalSettings);

        return configuration;
    }
}
