package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClassPathScanServiceImplTest {

    @InjectMocks
    private ClassPathScanServiceImpl loader = new ClassPathScanServiceImpl();

    @Mock
    ModuleService moduleService;

    @Test
    public void testGetBasePackages(){
        when(moduleService.getModuleList()).thenReturn(getModuleList(
                "ru.intertrust.gui.impl",
                "ru.intertrust.gui",
                "ru.intertrust.client",
                "ru.intertrust.server",
                "ru.intertrust.client.wiget",
                "ru.intertrust.server.impl",
                "ru.intertrust.server.impl.wiget"
                ));

        Set<String> result = ReflectionTestUtils.invokeMethod(loader, "getBasePackages");
        assertTrue(result.size() == 3);
        assertTrue(result.contains("ru.intertrust.gui"));
        assertTrue(result.contains("ru.intertrust.client"));
        assertTrue(result.contains("ru.intertrust.server"));
        assertFalse(result.contains("ru.intertrust.gui.impl"));
        assertFalse(result.contains("ru.intertrust.client.wiget"));
        assertFalse(result.contains("ru.intertrust.server.impl"));
        assertFalse(result.contains("ru.intertrust.server.impl.wiget"));
    }

    private List<ModuleConfiguration> getModuleList(String ... basePackages) {
        List<ModuleConfiguration> result = new ArrayList<>();
        for (String basePackage: basePackages) {
            ModuleConfiguration config = new ModuleConfiguration();
            config.setGuiComponentsPackages(Collections.singletonList(basePackage));
            result.add(config);
        }
        return result;
    }
}
