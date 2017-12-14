package ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("configuration.deployer.plugin")
public class ConfigurationDeployerPlugin extends Plugin implements IsActive {

    @Override
    public PluginView createView() {
        return new ConfigurationDeployerPluginView(this);
    }

    @Override
    public Component createNew() {
        return new ConfigurationDeployerPlugin();
    }

    @Override
    public <E extends PluginState> E getPluginState() {
        return null;
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        // do nothing
    }

    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);
        setDisplayActionToolBar(true);
        Application.getInstance().unlockScreen();
    }

    public List<AttachmentItem> getAttachmentItems() {
        ConfigurationDeployerPluginView deployerView = (ConfigurationDeployerPluginView)getView();
        return deployerView.getAttachmentItems();
    }

    public void clear() {
        ConfigurationDeployerPluginView deployerView = (ConfigurationDeployerPluginView)getView();
        deployerView.clear();
    }
}
