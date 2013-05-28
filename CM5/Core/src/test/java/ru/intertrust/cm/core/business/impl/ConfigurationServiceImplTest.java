package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
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
public class ConfigurationServiceImplTest {

    private ConfigurationServiceImpl configurationService;
    private DataStructureDAO dataStructureDAOMock;
    private Configuration config;

    @Before
    public void setUp() throws Exception {
        dataStructureDAOMock = mock(DataStructureDAO.class);
        configurationService = new ConfigurationServiceImpl();
        configurationService.setDataStructureDAO(dataStructureDAOMock);

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
