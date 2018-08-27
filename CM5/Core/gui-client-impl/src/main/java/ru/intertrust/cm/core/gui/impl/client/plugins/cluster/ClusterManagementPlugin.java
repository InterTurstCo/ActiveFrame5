package ru.intertrust.cm.core.gui.impl.client.plugins.cluster;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;


@ComponentName("cluster.management.plugin")
public class ClusterManagementPlugin extends Plugin {

    private EventBus eventBus = new SimpleEventBus();

    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public PluginView createView() {
        return new ClusterManagementPluginView(this, eventBus);
    }

    @Override
    public Component createNew() {
        return new ClusterManagementPlugin();
    }
}
