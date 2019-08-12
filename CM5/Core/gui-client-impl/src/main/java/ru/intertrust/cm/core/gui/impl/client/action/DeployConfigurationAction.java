package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.ConfigurationDeployedItem;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.panel.ConfigDeployingResultPopup;
import ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer.ConfigurationDeployerPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionData;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("deploy.configuration.action")
public class DeployConfigurationAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new DeployConfigurationAction();
    }

    @Override
    protected DeployConfigurationActionContext appendCurrentContext(ActionContext initialContext) {
        DeployConfigurationActionContext context = (DeployConfigurationActionContext) initialContext;
        ConfigurationDeployerPlugin plugin = (ConfigurationDeployerPlugin) getPlugin();
        context.setAttachmentItems(plugin.getAttachmentItems());
        context.setConfigType(plugin.getConfigType());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        DeployConfigurationActionData deployConfigurationActionData = (DeployConfigurationActionData) result;
        List<ConfigurationDeployedItem> configurationDeployedItems = deployConfigurationActionData.
                getConfigurationDeployedItems();
        final  ConfigurationDeployerPlugin plugin = (ConfigurationDeployerPlugin) getPlugin();
        plugin.clear();
        ConfigDeployingResultPopup configDeployingResultPopup = new ConfigDeployingResultPopup(configurationDeployedItems);
        configDeployingResultPopup.center();

    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return null;
    }
}

