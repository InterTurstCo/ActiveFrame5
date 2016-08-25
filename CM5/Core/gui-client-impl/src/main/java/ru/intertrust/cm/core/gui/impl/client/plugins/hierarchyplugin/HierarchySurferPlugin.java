package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchySurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchySurferPluginState;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 10:02
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("hierarchy.surfer.plugin")
public class HierarchySurferPlugin extends Plugin implements IsActive {

    private EventBus eventBus;

    public HierarchySurferPlugin(){
        eventBus = GWT.create(SimpleEventBus.class);
    }


    @Override
    public Component createNew() {
        return new HierarchySurferPlugin();
    }

    @Override
    public HierarchySurferPluginState getPluginState() {
        final HierarchySurferPluginData data = getInitialData();
        return (HierarchySurferPluginState) data.getPluginState().createClone();
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        final HierarchySurferPluginData data = getInitialData();
        data.setPluginState(pluginState);
    }

    @Override
    public PluginView createView() {
        return new HierarchySurferPluginView(this);
    }
}
