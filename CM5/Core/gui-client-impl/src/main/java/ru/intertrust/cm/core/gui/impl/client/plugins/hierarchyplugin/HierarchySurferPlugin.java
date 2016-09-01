package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;

import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchySurferConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.*;
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
public class HierarchySurferPlugin extends Plugin implements IsActive,PluginPanelSizeChangedEventHandler {

    private EventBus eventBus;
    private FormPlugin formPlugin;
    private HierarchyPlugin hierarchyPlugin;

    public HierarchySurferPlugin() {
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
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{PluginPanelSizeChangedEvent.TYPE};
    }

    @Override
    public PluginView createView() {
        return new HierarchySurferPluginView(this);
    }

    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

    @Override
    public void setInitialData(PluginData inData) {
        super.setInitialData(inData);
        HierarchySurferPluginData initialData = (HierarchySurferPluginData) inData;
        if (hierarchyPlugin == null) {
            hierarchyPlugin = ComponentRegistry.instance.get("hierarchy.plugin");
            hierarchyPlugin.setContainingHierarchyPlugin(this);
        }
        hierarchyPlugin.setConfig(((HierarchySurferConfig)getConfig()).getHierarchyPluginConfig());
        hierarchyPlugin.setInitialData(initialData.getHierarchyPluginData());
        hierarchyPlugin.setEventBus(eventBus);

        if (this.formPlugin == null) {
            this.formPlugin = ComponentRegistry.instance.get("form.plugin");
            this.formPlugin.setDisplayActionToolBar(false);
        }
        this.formPlugin.setInitialData(initialData.getFormPluginData());
        this.formPlugin.setLocalEventBus(eventBus);
    }

    @Override
    public void updateSizes() {
        int width = getOwner().getVisibleWidth();
        int height = getOwner().getVisibleHeight();
        hierarchyPlugin.getOwner().setVisibleWidth(width);
        hierarchyPlugin.getOwner().setVisibleHeight(height / 2);
        hierarchyPlugin.getView().onPluginPanelResize();
    }

    public HierarchyPlugin getHierarchyPlugin() {
        return hierarchyPlugin;
    }

    public FormPlugin getFormPlugin() {
        return formPlugin;
    }
}
