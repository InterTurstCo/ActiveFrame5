package ru.intertrust.cm.core.gui.impl.client.plugins.listplugin;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.listplugin.ListSurferConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.CancelSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.CancelSelectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.EditDoEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.EditDoEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.*;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListSurferPluginState;

/**
 * Created by Ravil on 11.04.2017.
 */
@ComponentName("list.surfer.plugin")
public class ListSurferPlugin extends Plugin implements IsActive,PluginPanelSizeChangedEventHandler,CancelSelectionEventHandler,
        OpenDomainObjectFormEventHandler,EditDoEventHandler {

    private EventBus eventBus;
    private FormPlugin formPlugin;
    private ListPlugin listPlugin;

    public ListSurferPlugin(){
        eventBus = GWT.create(SimpleEventBus.class);
        eventBus.addHandler(CancelSelectionEvent.TYPE,this);
        eventBus.addHandler(OpenDomainObjectFormEvent.TYPE,this);
        eventBus.addHandler(EditDoEvent.TYPE, this);
    }

    @Override
    public void setInitialData(PluginData inData) {
        super.setInitialData(inData);
        ListSurferPluginData initialData = (ListSurferPluginData)inData;
        if(listPlugin == null){
            listPlugin = ComponentRegistry.instance.get("list.plugin");
            listPlugin.setContainingListPlugin(this);
        }
        listPlugin.setConfig(((ListSurferConfig)getConfig()).getListPluginConfig());
        listPlugin.setInitialData(initialData.getListPluginData());
        listPlugin.setEventBus(eventBus);

        if (this.formPlugin == null) {
            this.formPlugin = ComponentRegistry.instance.get("form.plugin");
            this.formPlugin.setDisplayActionToolBar(false);
        }
        this.formPlugin.setInitialData(initialData.getFormPluginData());
        this.formPlugin.setLocalEventBus(eventBus);
    }

    @Override
    public void onOpenDomainObjectFormEvent(OpenDomainObjectFormEvent event) {
        final FormPluginState state = new FormPluginState();
        if (this.formPlugin.getFormPluginState().isEditable()) {
            state.setEditable(true);
            state.setToggleEdit(true);
        } else {
            state.setEditable(false);
            state.setToggleEdit(true);
        }
        state.setDomainObjectSource(DomainObjectSource.COLLECTION);
        state.setInCentralPanel(true);

        openFormFullScreen(event.getId(), state, ((ListSurferConfig)this.getConfig()).getFormViewerConfig(), null);
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
    public ListSurferPluginState getPluginState() {
        final ListSurferPluginData data = getInitialData();
        return (ListSurferPluginState) data.getPluginState().createClone();
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        final ListSurferPluginData data = getInitialData();
        data.setPluginState(pluginState);
    }

    @Override
    public void updateSizes() {
        int width = getOwner().getVisibleWidth();
        int height = getOwner().getVisibleHeight();
        listPlugin.getOwner().setVisibleWidth(width);
        listPlugin.getOwner().setVisibleHeight(height / 2);
        listPlugin.getView().onPluginPanelResize();
    }

    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
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
        newConfig.setFormViewerConfig(((ListSurferConfig)this.getConfig()).getFormViewerConfig());
        newFormPlugin.setConfig(newConfig);
        newFormPlugin.setPluginState(pluginState);
        formPluginPanel.open(newFormPlugin, false);
        this.formPlugin = newFormPlugin;
    }

    @Override
    public Component createNew() {
        return new ListSurferPlugin();
    }

    @Override
    public PluginView createView() {
        return new ListSurferPluginView(this);
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{PluginPanelSizeChangedEvent.TYPE};
    }

    public FormPlugin getFormPlugin() {
        return formPlugin;
    }

    public void setFormPlugin(FormPlugin formPlugin) {
        this.formPlugin = formPlugin;
    }

    public ListPlugin getListPlugin() {
        return listPlugin;
    }

    public void setListPlugin(ListPlugin listPlugin) {
        this.listPlugin = listPlugin;
    }

    @Override
    public void onEditDoEvent(EditDoEvent event) {
        final FormPluginState state = new FormPluginState();
        state.setEditable(true);
        state.setToggleEdit(true);
        state.setDomainObjectSource(DomainObjectSource.COLLECTION);
        state.setInCentralPanel(true);

        openFormFullScreen(event.getDoToEdit(), state, ((ListSurferConfig)this.getConfig()).getFormViewerConfig(), null);
    }
}
