package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEvent;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEventHandler;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.*;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.LINK_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SELECTED_IDS_KEY;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferPlugin extends Plugin implements IsActive, CollectionRowSelectedEventHandler,
        IsDomainObjectEditor, IsIdentifiableObjectList, PluginPanelSizeChangedEventHandler,
        HierarchicalCollectionEventHandler, OpenDomainObjectFormEventHandler, OpenHyperlinkInSurferEventHandler,
        DeleteCollectionRowEventHandler {

    private CollectionPlugin collectionPlugin;
    private FormPlugin formPlugin;
    // локальная шина событий
    private EventBus eventBus;

    /*
     * Конструктор плагина в котором
     * создается объект локальной шины событий
     */
    public DomainObjectSurferPlugin() {
        showBreadcrumbs = false;
        // устанавливается локальная шина событий
        eventBus = GWT.create(SimpleEventBus.class);
        eventBus.addHandler(CollectionRowSelectedEvent.TYPE, this);
        eventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);
        eventBus.addHandler(OpenDomainObjectFormEvent.TYPE, this);
        eventBus.addHandler(DeleteCollectionRowEvent.TYPE, this);
        Application.getInstance().addOpenDoInPluginHandlerRegistration(this);
    }

    @Override
    public PluginView createView() {
        return new DomainObjectSurferPluginView(this);
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{PluginPanelSizeChangedEvent.TYPE};
    }

    @Override
    public Component createNew() {
        return new DomainObjectSurferPlugin();
    }

    @Override
    public FormPluginState getFormPluginState() {
        return formPlugin.getFormPluginState();
    }

    @Override
    public DomainObjectSurferPluginState getPluginState() {
        final DomainObjectSurferPluginData data = getInitialData();
        return (DomainObjectSurferPluginState) data.getPluginState().createClone();
    }

    @Override

    public void setPluginState(PluginState pluginState) {
        final DomainObjectSurferPluginData data = getInitialData();
        data.setPluginState(pluginState);
    }

    public CollectionPlugin getCollectionPlugin() {
        return collectionPlugin;
    }

    public void setCollectionPlugin(CollectionPlugin collectionPlugin) {
        this.collectionPlugin = collectionPlugin;
    }

    public FormPlugin getFormPlugin() {
        return formPlugin;
    }

    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        final PluginPanel formPluginPanel = formPlugin.getOwner();
        final FormPlugin newFormPlugin = ComponentRegistry.instance.get("form.plugin");
        // после обновления формы ей снова "нужно дать" локальную шину событий
        newFormPlugin.setLocalEventBus(this.eventBus);
        final FormPluginConfig newConfig = new FormPluginConfig(event.getId());
        final FormPluginState pluginState = formPlugin.getPluginState();
        newConfig.setPluginState(pluginState);
        newConfig.setFormViewerConfig(getFormViewerConfig());
        newFormPlugin.setConfig(newConfig);
        newFormPlugin.setPluginState(pluginState);
        formPluginPanel.open(newFormPlugin, false);
        this.formPlugin = newFormPlugin;
        newFormPlugin.addViewCreatedListener(new FormPluginCreatedListener());
    }

    @Override
    public FormState getFormState() {
        return getFormPlugin().getFormState();
    }

    @Override
    public void setFormState(FormState formState) {
        getFormPlugin().setFormState(formState);
    }

    @Override
    public void setFormToolbarContext(final ToolbarContext toolbarContext) {
        getFormPlugin().setToolbarContext(toolbarContext);
        getView().updateActionToolBar();
    }

    @Override
    public DomainObject getRootDomainObject() {
        return getFormPlugin().getRootDomainObject();
    }

    @Override
    public void replaceForm(FormPluginConfig formPluginConfig) {
        final FormPlugin newPlugin = ComponentRegistry.instance.get("form.plugin");
        final FormPluginData data = formPlugin.getInitialData();
        final FormPluginState fpState = (FormPluginState) data.getPluginState();
        formPluginConfig.setPluginState(fpState);
        newPlugin.setConfig(formPluginConfig);
        newPlugin.addViewCreatedListener(new FormPluginCreatedListener());
        formPlugin.getOwner().open(newPlugin);
        formPlugin = newPlugin;
        formPlugin.setLocalEventBus(this.eventBus);
    }

    @Override
    public List<Id> getSelectedIds() {
        final CollectionPluginView cpView = (CollectionPluginView) collectionPlugin.getView();
        return cpView.getSelectedIds();
    }

    @Override
    public void setInitialData(PluginData inData) {
        super.setInitialData(inData);
        DomainObjectSurferPluginData initialData = (DomainObjectSurferPluginData) inData;

        if (this.collectionPlugin == null) {
            this.collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
            this.collectionPlugin.setContainingDomainObjectSurferPlugin(this);
        }
        this.collectionPlugin.setConfig(((DomainObjectSurferConfig) getConfig()).getCollectionViewerConfig());
        this.collectionPlugin.setInitialData(initialData.getCollectionPluginData());
        this.collectionPlugin.setLocalEventBus(eventBus);

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
        collectionPlugin.getOwner().setVisibleWidth(width);
        collectionPlugin.getOwner().setVisibleHeight(height / 2);
        collectionPlugin.getView().onPluginPanelResize();

    }

    @Override
    public boolean restoreHistory() {
        if (!collectionPlugin.restoreHistory()) {
            formPlugin.restoreHistory();
        }
        return false;
    }

    @Override
    public void fillHistoryData() {
        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) getConfig();
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        if (!manager.getSelectedIds().isEmpty()) {
            config.addHistoryValue(SELECTED_IDS_KEY, manager.getSelectedIds());
        }
        config.addHistoryValue(LINK_KEY, manager.getLink());
        config.getCollectionViewerConfig().addHistoryValues(
                manager.getValues(config.getCollectionViewerConfig().getCollectionRefConfig().getName()));
    }

    @Override
    public boolean isDirty() {
        return getFormPlugin().isDirty();
    }

    @Override
    public FormViewerConfig getFormViewerConfig() {
        return ((DomainObjectSurferConfig) this.getConfig()).getFormViewerConfig();
    }

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
        String currentCollectionName = getCollectionPlugin().getCollectionRowRequest().getCollectionName();
        ExpandHierarchicalCollectionData data = new ExpandHierarchicalCollectionData(
                event.getChildCollectionViewerConfigs(), event.getSelectedId(), currentCollectionName);

        final Command command = new Command("prepareHierarchicalCollectionData", "hierarchical.collection.builder", data);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert("Ошибка получения данных иерархической коллекции: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = getCollectionPlugin().getNavigationConfig();
                LinkUtil.addHierarchicalLinkToNavigationConfig(navigationConfig, link);
                FormViewerConfig formViewerConfig = LinkUtil.findHierarchyRootFormViewerConfig(getNavigationConfig());
                pluginConfig.setFormViewerConfig(formViewerConfig);
                Application.getInstance().getEventBus().fireEvent(
                        new NavigationTreeItemSelectedEvent(pluginConfig, link.getName(), navigationConfig));
            }
        });
    }

    @Override
    public void onOpenDomainObjectFormEvent(OpenDomainObjectFormEvent event) {
        final FormPluginState state = new FormPluginState();
        if (this.getFormPluginState().isEditable()) {
            state.setEditable(true);
            state.setToggleEdit(true);
        } else {
            state.setEditable(false);
            state.setToggleEdit(true);
        }
        state.setDomainObjectSource(DomainObjectSource.COLLECTION);
        state.setInCentralPanel(true);

        openFormFullScreen(event.getId(), state, getFormViewerConfig(), null);
    }

    @Override
    public void refresh() {
        collectionPlugin.refresh();
    }

    @Override
    public void clearHandlers() {
        super.clearHandlers();
        collectionPlugin.clearHandlers();
        formPlugin.clearHandlers();

    }

    @Override
    public void onOpenHyperlinkInSurfer(OpenHyperlinkInSurferEvent event) {
        final FormPluginState state = new FormPluginState();
        state.setEditable(event.isEditable());
        state.setToggleEdit(true);
        state.setDomainObjectSource(DomainObjectSource.HYPERLINK);
        state.setInCentralPanel(true);

        FormViewerConfig formViewerConfig = new FormViewerConfig();
        formViewerConfig.setFormMappingConfigList(LinkedFormMappingConfig.toFormMappingConfigList(event.getLinkedFormMappings()));
        openFormFullScreen(event.getId(), state, formViewerConfig, event.getPluginCloseListener());
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
    public void deleteCollectionRow(DeleteCollectionRowEvent event) {
        CollectionPluginView cpView = (CollectionPluginView) collectionPlugin.getView();
        cpView.delCollectionRow(event.getId());
        List<Id> selected = cpView.getSelectedIds();
        FormPluginConfig config;
        if (selected.isEmpty()) {
            final String domainObjectType = getRootDomainObject().getTypeName();
            config = new FormPluginConfig(domainObjectType);
        } else {
            config = new FormPluginConfig(selected.get(0));
        }
        config.setPluginState(getFormPluginState());
        config.setFormViewerConfig(getFormViewerConfig());
        Plugin pluginToHandle = event.getPlugin();
        if (event.getPlugin() instanceof FormPlugin) {
            pluginToHandle.getOwner().closeCurrentPlugin();

        }
        replaceForm(config);

    }


    private class FormPluginCreatedListener implements PluginViewCreatedEventListener {

        @Override
        public void onViewCreation(PluginViewCreatedEvent source) {
            Application.getInstance().unlockScreen();
            ((DomainObjectSurferPluginData) getInitialData())
                    .setFormPluginData((FormPluginData) source.getPlugin().getInitialData());
            getView().updateActionToolBar();
            UserSettingsUtil.storeCurrentNavigationLink();
        }
    }

}
