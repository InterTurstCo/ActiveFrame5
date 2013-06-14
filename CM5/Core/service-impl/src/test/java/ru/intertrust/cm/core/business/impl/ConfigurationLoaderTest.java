package ru.intertrust.cm.core.business.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessObjectsConfigurationLogicalValidator;

import static org.mockito.Mockito.verify;

/**
 * @author vmatsukevich
 *         Date: 5/28/13
 *         Time: 4:12 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLoaderTest {

    @InjectMocks
    private ConfigurationLoader configurationLoader = new ConfigurationLoader();
    @Mock
    private ConfigurationService configurationService;
    @Mock
    BusinessObjectsConfigurationLogicalValidator logicalValidator;

    @Test
    public void testLoad() throws Exception {
        configurationLoader.load();

        verify(logicalValidator).validate();
        verify(configurationService).loadConfiguration();
    }
}
