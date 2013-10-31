package ru.intertrust.cm.core.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.intertrust.cm.core.config.model.base.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * @throws Exception
     */
    @Before
    public void SetUp() throws Exception {
        suppress(method(NavigationPanelLogicalValidator.class, "validatePluginHandlers"));

    }

    @Test
    public void validateCorrectNavigationPanel() throws Exception {

        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);
        NavigationPanelLogicalValidator panelValidator = new NavigationPanelLogicalValidator(configurationExplorer);
        panelValidator.validate();
    }

    @Test
    public void validateIncorrectNavigationPanel() throws Exception {

        String exceptionMessage = ("Configuration of "
        + "navigation panel with name 'panel' was validated with errors.Count: 2 Content:\n"
        + "Child link to open is not found for link with name 'Administration'\n"
        + "Child link to open is not found for link with name 'Documents In Work'\n");
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH);
        NavigationPanelLogicalValidator panelValidator = new NavigationPanelLogicalValidator();
        panelValidator.setConfigurationExplorer(configurationExplorer);
        try {
        panelValidator.validate();
        } catch(ConfigurationException e) {
           assertEquals(exceptionMessage, e.getMessage());
        }

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); 
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH, GLOBAL_XML_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl(configuration);

        configurationExplorer.build();
        return configurationExplorer;
    }

}

