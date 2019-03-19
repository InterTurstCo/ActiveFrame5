package ru.intertrust.cm.core.business.impl;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ConfigurationControlServiceImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationControlServiceImplTest extends TestCase {

    @InjectMocks
    private ConfigurationControlServiceImpl configurationControlService = new ConfigurationControlServiceImpl();

    @Mock
    private ConfigurationExplorer configurationExplorer;

    @Mock
    private ConfigurationSerializer configurationSerializer;

    /*@Test
    public void testUpdateConfiguration() throws Exception {
        Configuration configuration = createConfiguration();
        ConfigurationExplorerImpl configurationExplorerImpl = new ConfigurationExplorerImpl(configuration);

        Configuration newConfiguration = createConfiguration();
        DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) newConfiguration.getConfigurationList().get(0);

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("New_Field");
        registrationNumber.setLength(256);
        registrationNumber.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(registrationNumber);

        GlobalSettingsConfig globalSettingsConfig = (GlobalSettingsConfig) newConfiguration.getConfigurationList().get(2);
        globalSettingsConfig.getSqlTrace().setResolveParameters(false);

        String newConfigurationString = ConfigurationSerializer.serializeConfiguration(newConfiguration);

        when(configurationExplorer.getConfig(GlobalSettingsConfig.class, GlobalSettingsConfig.NAME)).
                thenReturn(configurationExplorerImpl.getConfig(GlobalSettingsConfig.class, GlobalSettingsConfig.NAME));
        when(configurationSerializer.deserializeLoadedConfiguration(newConfigurationString, false)).
                thenReturn(newConfiguration);

        configurationControlService.updateConfiguration(newConfigurationString);

        verify(configurationExplorer, never()).updateConfig(domainObjectTypeConfig);
        verify(configurationExplorer, times(1)).updateConfig(globalSettingsConfig);
    }*/

    @Test
    public void testRestartRequiredForFullUpdate() throws Exception {
        Configuration configuration = createConfiguration();
        ConfigurationExplorerImpl configurationExplorerImpl = new ConfigurationExplorerImpl(configuration);

        Configuration newConfiguration = createConfiguration();

        String newConfigurationString = ConfigurationSerializer.serializeConfiguration(newConfiguration);

        when(configurationExplorer.getConfig(GlobalSettingsConfig.class, GlobalSettingsConfig.NAME)).
                thenReturn(configurationExplorerImpl.getConfig(GlobalSettingsConfig.class, GlobalSettingsConfig.NAME));
        when(configurationExplorer.getDomainObjectTypeConfig("Outgoing_Document")).
                thenReturn(configurationExplorerImpl.getDomainObjectTypeConfig("Outgoing_Document"));
        when(configurationExplorer.getDomainObjectTypeConfig("Employee")).
                thenReturn(configurationExplorerImpl.getDomainObjectTypeConfig("Employee"));
        when(configurationSerializer.deserializeLoadedConfiguration(newConfigurationString)).
                thenReturn(newConfiguration);

        assertFalse(configurationControlService.restartRequiredForFullUpdate(newConfigurationString));

        DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) newConfiguration.getConfigurationList().get(0);

        StringFieldConfig registrationNumber = new StringFieldConfig();
        registrationNumber.setName("New_Field");
        registrationNumber.setLength(256);
        registrationNumber.setNotNull(true);
        domainObjectTypeConfig.getFieldConfigs().add(registrationNumber);

        newConfigurationString = ConfigurationSerializer.serializeConfiguration(newConfiguration);

        when(configurationSerializer.deserializeLoadedConfiguration(newConfigurationString)).
                thenReturn(newConfiguration);

        assertTrue(configurationControlService.restartRequiredForFullUpdate(newConfigurationString));
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
}
