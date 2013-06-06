package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * @author vmatsukevich
 *         Date: 5/28/13
 *         Time: 4:12 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLoaderTest {

    private static final String CONFIG_PATH = "test-config/business-objects.xml";
    private static final String COLLECTIONS_CONFIG_PATH = "test-config/collections.xml";

    @InjectMocks
    private ConfigurationLoader configurationLoader = new ConfigurationLoader();
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private ConfigurationValidator configurationValidator;

    @Before
    public void setUp() throws Exception {
        configurationLoader.setConfigurationFilePath(CONFIG_PATH);
        configurationLoader.setCollectionsConfigurationFilePath(COLLECTIONS_CONFIG_PATH);
    }

    @Test
    public void testLoad() throws Exception {
        configurationLoader.load();
        Configuration configuration = configurationLoader.getConfiguration();
        
        verify(configurationValidator).validate();
        verify(configurationService).loadConfiguration(configuration);

        assertNotNull(configuration);
        assertEquals(configuration.getBusinessObjectConfigs().size(), 4);

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(configuration, "Employee");
        assertNotNull(businessObjectConfig);

        assertEquals(businessObjectConfig.getFieldConfigs().size(), 6);
        assertEquals(businessObjectConfig.getUniqueKeyConfigs().size(), 1);
        assertEquals(businessObjectConfig.getParentConfig(), "Person");
    }
}
