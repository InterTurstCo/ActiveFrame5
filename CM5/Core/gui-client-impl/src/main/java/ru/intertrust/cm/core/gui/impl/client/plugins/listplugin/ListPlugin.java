package ru.intertrust.cm.core.gui.impl.client.plugins.listplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeStateEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by Ravil on 11.04.2017.
 */
@ComponentName("list.plugin")
public class ListPlugin extends Plugin {
    private ListSurferPlugin containingListPlugin;
    private EventBus eventBus = new SimpleEventBus();
    private ListPluginView pView;

    @Override
    public Component createNew() {
        return new ListPlugin();
    }

    @Override
    public PluginView createView() {
        pView = new ListPluginView(this, eventBus);
        return pView;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setContainingListPlugin(ListSurferPlugin containingListPlugin) {
        this.containingListPlugin = containingListPlugin;
        this.eventBus = containingListPlugin.getLocalEventBus();
    }
}
