package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionDisplayType;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.ConfigurationDeployerPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("configuration.deployer.plugin")
public class ConfigurationDeployerHandler extends PluginHandler {
    public PluginData initialize(Dto config) {
        ConfigurationDeployerPluginData pluginData = new ConfigurationDeployerPluginData();
        List<ActionContext> activeContexts = new ArrayList<ActionContext>();
        ActionConfig actionConfig = new ActionConfig("deploy.configuration.action", "deploy.configuration.action");
        actionConfig.setText("Загрузить конфигурацию");
        actionConfig.setDisplay(ActionDisplayType.toggleButton);
        actionConfig.setImageClass("configurationUploader");
        activeContexts.add(new DeployConfigurationActionContext(actionConfig));
        final ToolbarContext toolbarContext = new ToolbarContext();
        toolbarContext.setContexts(activeContexts, ToolbarContext.FacetName.LEFT);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }
}
