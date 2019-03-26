package ru.intertrust.cm.core.business.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.config.DomainObjectLogicalValidator;

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
    private ConfigurationLoadService configurationLoadService;
    @Mock
    DomainObjectLogicalValidator logicalValidator;

    @Test
    public void testLoad() throws Exception {
        configurationLoader.load();
        verify(configurationLoadService).loadConfiguration();
    }
}
