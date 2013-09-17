package ru.intertrust.cm.core.gui.impl.client.plugins.searchpanel;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("search.plugin")
public class SearchPlugin extends Plugin {
    @Override
    public PluginView createView() {
        return new SearchPluginView(this);
    }

    @Override
    public Component createNew() {
        return new SearchPlugin();
    }
}
