package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin {
    @Override
    public PluginView createView() {
        return new NavigationTreePluginView(this);

    }
    @Override
    public Component createNew() {
        return new NavigationTreePlugin();
    }
}
