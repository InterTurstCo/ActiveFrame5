package ru.intertrust.cm.core.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(NavigationPanelLogicalValidator.class)
public class NavigationPanelLogicalValidatorTest {

    private static final String NAVIGATION_PANEL_XML_PATH = "config/navigation-panel-test.xml";
    private static final String NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH =
            "config/navigation-panel-with-errors.xml";

    private static final String GLOBAL_XML_PATH = "config/global-test.xml";

    /**
     * Вызов метода validatePluginHandlers исключается на время тестов
     * Для корректной работы validatePluginHandlers требуется спринг контекст
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        suppress(method(NavigationPanelLogicalValidator.class, "validatePluginHandlers"));

    }

    @Test
    public void validateCorrectNavigationPanel() throws Exception {

        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);
        NavigationPanelLogicalValidator panelValidator = new NavigationPanelLogicalValidator(configurationExplorer);
        List<LogicalErrors> errors = panelValidator.validate();
        assertEquals(0, errors.size());
    }

    @Test
    public void validateIncorrectNavigationPanel() throws Exception {

        String exceptionMessage = ("Configuration of navigation panel with name 'panel' was validated with errors.Count: 3 Content:\n" +
                "Child link to open is not found for link with name 'Administration'\n" +
                "Duplicate link name 'Cities' was found\n" +
                "Child link to open is not found for link with name 'Documents In Work'");
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH);
        NavigationPanelLogicalValidator panelValidator = new NavigationPanelLogicalValidator();
        panelValidator.setConfigurationExplorer(configurationExplorer);

        List<LogicalErrors> errors = panelValidator.validate();
        assertEquals(1, errors.size());
        assertEquals(3, errors.get(0).getErrorCount());
        assertEquals(exceptionMessage, errors.get(0).toString());


    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(configPath));

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        return new ConfigurationExplorerImpl(configuration,true);
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        ModuleService result = new ModuleService();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        conf.getConfigurationPaths().add(DOMAIN_OBJECTS_CONFIG_PATH);
        conf.getConfigurationPaths().add(GLOBAL_XML_PATH);
        conf.getConfigurationPaths().add(configPath);
        conf.getConfigurationPaths().add(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH);        
        conf.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        final URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);
        return result;
    }
}

