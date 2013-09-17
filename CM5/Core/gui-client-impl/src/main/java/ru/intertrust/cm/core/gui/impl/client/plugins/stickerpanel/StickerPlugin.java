package ru.intertrust.cm.core.gui.impl.client.plugins.stickerpanel;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("sticker.plugin")
public class StickerPlugin extends Plugin {
    @Override
    public PluginView createView() {
        return new StickerPluginView(this);
    }

    @Override
    public Component createNew() {
        return new StickerPlugin();
    }
}
