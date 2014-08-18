package ru.intertrust.cm.core.gui.impl.client.form;

import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;

/**
 * @author Denis Mitavskiy
 *         Date: 28.07.2014
 *         Time: 15:33
 */
public abstract class WidgetsContainer {
    protected Plugin plugin;

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public abstract <T extends BaseWidget> T getWidget(String id);
}
