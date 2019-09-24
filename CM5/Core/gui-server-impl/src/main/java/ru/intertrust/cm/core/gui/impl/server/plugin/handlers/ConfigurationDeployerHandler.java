package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ConfigurationDeployer;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.ConfigurationDeployerPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("configuration.deployer.plugin")
public class ConfigurationDeployerHandler extends PluginHandler {

    @Autowired
    private ActionService actionService;
    
    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    @Autowired
    private List<ConfigurationDeployer> deployers;
    
    public PluginData initialize(Dto config) {
        ConfigurationDeployerPluginData pluginData = new ConfigurationDeployerPluginData();

        // сформировать список поддерживаемых типов конфигураций
        List<DeployConfigType> configTypes = new ArrayList<DeployConfigType>();
        for (ConfigurationDeployer deployer : deployers) {
            configTypes.add(deployer.getDeployConfigType());
        }
        pluginData.setConfigTypes(configTypes);
        
        ToolbarContext toolbarContext = new ToolbarContext();
        ToolBarConfig defaultToolbarConfig = actionService.getDefaultToolbarConfig("configuration.deployer.plugin",
                GuiContext.getUserLocale());
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), new HashMap<String, Object>());
        toolbarContext.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }
}
