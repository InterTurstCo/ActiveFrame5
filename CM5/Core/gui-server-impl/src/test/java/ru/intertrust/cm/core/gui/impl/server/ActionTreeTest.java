package ru.intertrust.cm.core.gui.impl.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.gui.action.AbstractActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * @author Sergey.Okolot
 *         Created on 24.04.2014 9:31.
 */
public class ActionTreeTest {
    private ActionTree instance;
    private ConfigurationExplorer configurationExplorer;

    @Before
    public void beforeTest() throws Exception {
        configurationExplorer =
                createConfigurationExplorer("ru/intertrust/cm/core/gui/impl/server/action-tree-junit.xml");
        final Collection<ActionEntryConfig> actions = configurationExplorer.getConfigs(ActionEntryConfig.class);
        instance = new ActionTree(actions);
    }

//    @Test
    public void testAddAction() throws Exception {
        final ToolBarConfig toolBarConfig = configurationExplorer.getConfig(ToolBarConfig.class, "1");
        for (AbstractActionEntryConfig actionEntryConfig : toolBarConfig.getActions()) {
            instance.addAction(actionEntryConfig);
        }
        final Collection result = instance.getActions();
        showTree(instance.getRoot(), 0);
    }

    private void showTree(ActionTree.ActionNode parent, int level) {
        final Collection<ActionTree.ActionNode> nodes = parent.getChildren();
        if (!nodes.isEmpty()) {
            for (ActionTree.ActionNode node : nodes) {
                for (int index = 0; index < level; index++) {
                    System.out.print("---");
                }
                System.out.println(node);
                showTree(node, level + 1);
            }
        }
    }


    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        configurationSerializer.setModuleService(createModuleService(configPath));

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        System.out.println("--------------------------------->  start process");
        for (TopLevelConfig tlc : configuration.getConfigurationList()) {
            System.out.println("---------------------------------> " + tlc);

        }
        return new ConfigurationExplorerImpl(configuration);
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        ModuleService result = new ModuleService();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        conf.getConfigurationPaths().add(configPath);
        conf.setConfigurationSchemaPath("config/configuration.xsd");
        final URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);
        return result;
    }

}
