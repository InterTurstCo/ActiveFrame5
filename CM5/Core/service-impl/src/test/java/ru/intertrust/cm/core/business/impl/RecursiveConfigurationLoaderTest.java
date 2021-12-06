package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RecursiveConfigurationLoaderTest {

    @InjectMocks
    private RecursiveConfigurationLoader recursiveConfigurationLoader = new RecursiveConfigurationLoader();
    @Mock
    private DataStructureDao dataStructureDao;
    @Mock
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private ConfigurationExplorerImpl configExplorer;

    @Before
    public void setUp() throws Exception {
        ConfigurationClassesCache.getInstance().build();

        Configuration configuration = createConfiguration();
        configExplorer = new ConfigurationExplorerImpl(configuration);
        configExplorer.init();
    }

    @Test
    public void testLoadConfigurationFirstTime() throws Exception {
        recursiveConfigurationLoader.load(configExplorer);

        verify(dataStructureDao).createServiceTables();
        verify(dataStructureDao, times(4)).createTable(any(DomainObjectTypeConfig.class), any(Boolean.class));
        verify(dataStructureDao, times(4)).createSequence(any(DomainObjectTypeConfig.class));
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
