package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("item.viewer.plugin")
public class ItemViewerPlugin extends Plugin {
    @Override
    public PluginView createView() {
        return new ItemViewerPluginView(this);
    }

    @Override
    public Component createNew() {
        return new ItemViewerPlugin();
    }
}
