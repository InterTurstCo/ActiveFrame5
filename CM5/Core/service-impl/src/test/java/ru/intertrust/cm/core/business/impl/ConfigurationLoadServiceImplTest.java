package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.InitializationLockDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.ejb.EJBContext;

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
    private InitializationLockDao initializationLockDao;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private MigrationService migrationService;
    @Mock
    private ConfigurationSerializer configurationSerializer;
    @Mock
    private EJBContext ejbContext;
    @Mock
    private RecursiveConfigurationLoader recursiveConfigurationLoader;
    @Mock
    private RecursiveConfigurationMerger recursiveConfigurationMerger;
    @Mock
    private ConfigurationExtensionProcessor configurationExtensionProcessor;
    @Mock
    private CrudService crudService;
    @Mock
    private DataStructureDao dataStructureDao;
    @Mock
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Mock
    private ApplicationContext context;

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        new SpringApplicationContext().setApplicationContext(context);

        ConfigurationClassesCache.getInstance().build();

        configuration = createConfiguration();
        configurationService.setConfigurationExplorer(new ConfigurationExplorerImpl(configuration));
        when(ejbContext.getUserTransaction()).thenReturn(new MockUserTransaction());
    }

    @Test
    public void testLoadConfigurationUpdated() throws Exception {
        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);
        when(configurationSerializer.deserializeLoadedConfiguration(configurationString)).thenReturn(configuration);

        Configuration updatedConfiguration = createConfiguration();
        ConfigurationExplorerImpl configExplorer = new ConfigurationExplorerImpl(updatedConfiguration);

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

        when(context.getBean("recursiveConfigurationLoader")).thenReturn(recursiveConfigurationLoader);
        when(crudService.createDomainObject(anyString())).thenReturn(new GenericDomainObject());
        configurationService.loadConfiguration();
    }

    @Test
    public void testLoadConfigurationNoUpdate() throws Exception {
        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        when(configurationDao.readLastSavedConfiguration()).thenReturn(configurationString);
        when(configurationSerializer.deserializeLoadedConfiguration(configurationString)).thenReturn(configuration);
        when(context.getBean("recursiveConfigurationMerger")).thenReturn(recursiveConfigurationMerger);

        configurationService.updateConfiguration();

        verify(recursiveConfigurationMerger, never()).merge(any(ConfigurationExplorer.class), any((ConfigurationExplorer.class)));
        verify(configurationDao, never()).save(anyString());
    }

    @Test
    public void testLoadConfigurationFirstTime() throws Exception {
        when(context.getBean("recursiveConfigurationLoader")).thenReturn(recursiveConfigurationLoader);
        when(crudService.createDomainObject(anyString())).thenReturn(new GenericDomainObject());

        configurationService.loadConfiguration();

        verify(recursiveConfigurationLoader).load(any(ConfigurationExplorer.class));
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

        configuration.getConfigurationList().add(createOutgoingDocument());
        configuration.getConfigurationList().add(createEmployee());
        configuration.getConfigurationList().add(globalSettings);

        return configuration;
    }
}
