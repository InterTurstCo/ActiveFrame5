package ru.intertrust.cm.core.config.form;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.impl.ModuleServiceImpl;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 15:13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/beans-test.xml"})
public abstract class AbstractConfigProcessingTest {

    @Autowired
    private ApplicationContext context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(configPath);
        Configuration config = configurationSerializer.deserializeConfiguration();
        return new ConfigurationExplorerImpl(config, context,true);
    }

    private ConfigurationSerializer createConfigurationSerializer(String confPath) throws Exception {
        ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(confPath));

        return configurationSerializer;
    }

    private ModuleService createModuleService(String confPath) throws MalformedURLException {
        URL moduleUrl = getClass().getClassLoader().getResource(".");

        ModuleServiceImpl result = new ModuleServiceImpl();
        ModuleConfiguration confCore = new ModuleConfiguration();
        confCore.setName("core");
        result.getModuleList().add(confCore);
        confCore.setConfigurationPaths(new ArrayList<String>());
        confCore.getConfigurationPaths().add(confPath);
        confCore.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        confCore.setModuleUrl(moduleUrl);

        return result;
    }
}
