package ru.intertrust.testmodule;

import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;

import static org.junit.Assert.assertTrue;

public class FullConfigurationSerializerTest {

    public static final String FORGOT_EQUALS_HASH_CODE_ERROR_MESSAGE = "You obviously forgot to define correct equals() and hashCode() in your new configuration classes!";

    @Test
    public void testConfigurationEquality() throws Exception {
        ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService());
        final Configuration configuration1 = configurationSerializer.deserializeConfiguration();
        Configuration configuration2 = configurationSerializer.deserializeConfiguration();
        assertTrue(FORGOT_EQUALS_HASH_CODE_ERROR_MESSAGE, configuration1.equals(configuration2));

        configuration2 = configurationSerializer.deserializeLoadedConfiguration(ConfigurationSerializer.serializeConfiguration(configuration1));
        assertTrue(FORGOT_EQUALS_HASH_CODE_ERROR_MESSAGE, configuration1.equals(configuration2));
    }

    private ModuleService createModuleService() throws MalformedURLException {
        ModuleService moduleService = new ModuleService();
        moduleService.init();

        return moduleService;
    }
}
