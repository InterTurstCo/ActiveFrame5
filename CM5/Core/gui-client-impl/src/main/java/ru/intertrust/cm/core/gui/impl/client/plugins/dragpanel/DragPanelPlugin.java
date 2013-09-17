package ru.intertrust.cm.core.gui.impl.client.plugins.dragpanel;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("dragpanel.plugin")
public class DragPanelPlugin extends Plugin {
    @Override
    public PluginView createView() {
        return new DragPanelPluginView(this);
    }

    @Override
    public Component createNew() {
        return new DragPanelPlugin();
    }
}
