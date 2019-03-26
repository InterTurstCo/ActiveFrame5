package ru.intertrust.cm.core.business.impl;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.config.DomainObjectLogicalValidator;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * @author vmatsukevich
 *         Date: 5/28/13
 *         Time: 4:12 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLoaderTest {

    @InjectMocks
    private ConfigurationLoader configurationLoader;
    @Mock
    private ConfigurationLoadService configurationLoadService;
    @Mock
    private DomainObjectLogicalValidator logicalValidator;
    @Mock
    private SpringApplicationContext applicationContext;    

    @Before
    public void init() {
        new SpringApplicationContext().setApplicationContext(null);
    }
    
    @Test
    public void testLoad() throws Exception {
        configurationLoader.load();
        verify(configurationLoadService).loadConfiguration();
    }
}
