package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.impl.ConfigurationLoader;
import ru.intertrust.cm.core.business.impl.ConfigurationServiceImpl;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;
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
    @InjectMocks
    private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl();
    @Mock
    private DataStructureDAO dataStructureDAOMock;
    @Mock
    private AuthenticationService authenticationService;

    private Configuration config;

    @Before
    public void setUp() throws Exception {
        config = new ConfigurationLoader().serializeConfiguration("config/business-objects.xml");
        assertNotNull(config); // проверяем, что конфигурация сериализована из файла
    }

    @Test
    public void testLoadConfigurationWhenLoaded() throws Exception {
        when(dataStructureDAOMock.countTables()).thenReturn(10);
        configurationService.loadConfiguration(config);

        verify(dataStructureDAOMock).countTables();
        verify(dataStructureDAOMock, never()).createServiceTables();
        verify(dataStructureDAOMock, never()).createTable(Matchers.<BusinessObjectConfig>anyObject());
    }

    @Test
    public void testLoadConfigurationWhenNotLoaded() throws Exception {
        when(dataStructureDAOMock.countTables()).thenReturn(0);
        configurationService.loadConfiguration(config);

        verify(dataStructureDAOMock).countTables();
        verify(dataStructureDAOMock).createServiceTables();
        verify(dataStructureDAOMock, times(config.getBusinessObjectConfigs().size())).createTable(Matchers.<BusinessObjectConfig>anyObject());
    }
}
