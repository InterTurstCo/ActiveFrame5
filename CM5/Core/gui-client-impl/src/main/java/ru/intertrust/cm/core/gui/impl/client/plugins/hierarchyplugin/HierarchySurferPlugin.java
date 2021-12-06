package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchySurferConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.impl.client.*;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.CancelSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.CancelSelectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.EditDoEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.EditDoEventHandler;
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
public class HierarchySurferPlugin extends Plugin implements IsActive,PluginPanelSizeChangedEventHandler,CancelSelectionEventHandler,
        OpenDomainObjectFormEventHandler, EditDoEventHandler {

    private EventBus eventBus;
    private FormPlugin formPlugin;
    private HierarchyPlugin hierarchyPlugin;

    public HierarchySurferPlugin() {
        eventBus = GWT.create(SimpleEventBus.class);
        eventBus.addHandler(CancelSelectionEvent.TYPE,this);
        eventBus.addHandler(OpenDomainObjectFormEvent.TYPE,this);
        eventBus.addHandler(EditDoEvent.TYPE, this);
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
            this.formPlugin = ComponentRegistry.instance.get("empty.form.plugin");
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

    @Override
    public void onCancelSelectionEvent(CancelSelectionEvent event) {
        final PluginPanel formPluginPanel = formPlugin.getOwner();
        final FormPlugin newFormPlugin = ComponentRegistry.instance.get("form.plugin");
        // после обновления формы ей снова "нужно дать" локальную шину событий
        newFormPlugin.setLocalEventBus(this.eventBus);
        final FormPluginConfig newConfig = new FormPluginConfig(event.getRowId());
        final FormPluginState pluginState = formPlugin.getPluginState();
        newConfig.setPluginState(pluginState);
        newConfig.setFormViewerConfig(((HierarchySurferConfig)this.getConfig()).getFormViewerConfig());
        newFormPlugin.setConfig(newConfig);
        newFormPlugin.setPluginState(pluginState);
        formPluginPanel.open(newFormPlugin, false);
        this.formPlugin = newFormPlugin;
    }

    @Override
    public void onOpenDomainObjectFormEvent(OpenDomainObjectFormEvent event) {
        final FormPluginState state = new FormPluginState();
       if (this.formPlugin != null && this.formPlugin.getFormPluginState() != null &&
               this.formPlugin.getFormPluginState().isEditable()) {
            state.setEditable(true);
            state.setToggleEdit(true);
        } else {
            state.setEditable(false);
            state.setToggleEdit(true);
        }
        state.setDomainObjectSource(DomainObjectSource.COLLECTION);
        state.setInCentralPanel(true);

        openFormFullScreen(event.getId(), state, ((HierarchySurferConfig)this.getConfig()).getFormViewerConfig(), null);
    }

    private void openFormFullScreen(Id id, FormPluginState state, FormViewerConfig formViewerConfig, PluginCloseListener pluginCloseListener) {
        final FormPluginConfig config = new FormPluginConfig(id);
        config.setPluginState(state);
        config.setFormViewerConfig(formViewerConfig);

        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        formPlugin.setLocalEventBus(getLocalEventBus());
        if (pluginCloseListener != null) {
            formPlugin.addPluginCloseListener(pluginCloseListener);
        }
        Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));
    }

    @Override
    public void onEditDoEvent(EditDoEvent event) {
        final FormPluginState state = new FormPluginState();
        state.setEditable(true);
        state.setToggleEdit(true);
        state.setDomainObjectSource(DomainObjectSource.COLLECTION);
        state.setInCentralPanel(true);

        openFormFullScreen(event.getDoToEdit(), state, ((HierarchySurferConfig)this.getConfig()).getFormViewerConfig(), null);
    }
}
