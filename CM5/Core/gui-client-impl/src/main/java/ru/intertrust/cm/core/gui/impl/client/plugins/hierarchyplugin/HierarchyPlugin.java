package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyPluginConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 14:20
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("hierarchy.plugin")
public class HierarchyPlugin extends Plugin {

    private EventBus eventBus = new SimpleEventBus();

    @Override
    public PluginView createView() {
        HierarchyPluginConfig hierarchyPluginConfig = (HierarchyPluginConfig)getConfig();
        return new HierarchyPluginView(this, eventBus);
    }

    @Override
    public Component createNew() {
        return new HierarchyPlugin();
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
