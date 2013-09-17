package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("collection.viewer.plugin")
public class CollectionViewerPlugin extends Plugin {
    @Override
    public PluginView createView() {

        return new CollectionViewerPluginView(this);
    }

    @Override
    public Component createNew() {
        return new CollectionViewerPlugin();
    }

}
