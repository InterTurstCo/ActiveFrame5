package ru.intertrust.cm.core.config;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.base.Configuration;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidatorTest {
    final static Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidatorTest.class);
    private static final String NAVIGATION_PANEL_XML_PATH =
            "config/navigation-panel-test.xml";

    private static final String NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH =
            "config/navigation-panel-invalid-child-to-open.xml";

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);

    }

    @Test
    public void testValidateInvalidChildToOpen() throws Exception {
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH);

    /*    String logMsg = builder.toString();
        Assert.assertNotNull(logMsg);
        System.out.println("-----------------logMsg begin:\n" + logMsg + "\n ---------End");
        Assert.assertTrue(logMsg.contains("Child link to open is not found for name 'Administration'")); */
    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); 
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        return configurationExplorer;
    }
     private void findRequiredLink(ConfigurationExplorer configurationExplorer) {
         NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class, "panel");
         List<LinkConfig> linkConfigList =  navigationConfig.getLinkConfigList();
         for(LinkConfig link: linkConfigList) {
             String childToOpen = link.getChildToOpen();
             if(childToOpen != null){
                 List<ChildLinksConfig> childLinksConfigs = link.getChildLinksConfigList();
             if(!findLinkByName(childLinksConfigs, childToOpen)) {
                 logger.error("Child link to open is not found for name '" + childToOpen + "'");
             }
             }
         }
         logger.info("All child links to open are described in configuration");
     }
    private boolean findLinkByName(List<ChildLinksConfig> childLinksConfigs, String name) {
        if(childLinksConfigs != null && !childLinksConfigs.isEmpty()) {
              for(ChildLinksConfig childLink: childLinksConfigs) {
                  for(LinkConfig link: childLink.getLinkConfigList()) {
                      String linkName = link.getName();
                      if(name.equalsIgnoreCase(linkName)) {
                        return true;
                      }
                  }
              }
        }
        return false;
    }

}

