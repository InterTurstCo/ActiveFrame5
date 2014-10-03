package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginState;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.model.plugin.IsIdentifiableObjectList;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;
import java.util.logging.Logger;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.LINK_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SELECTED_IDS_KEY;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferPlugin extends Plugin implements IsActive, CollectionRowSelectedEventHandler,
        IsDomainObjectEditor, IsIdentifiableObjectList, PluginPanelSizeChangedEventHandler,
        HierarchicalCollectionEventHandler {

    private CollectionPlugin collectionPlugin;
    private FormPlugin formPlugin;
    // локальная шина событий
    private EventBus eventBus;

    static Logger log = Logger.getLogger("domain.object.surfer.plugin");


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
        return formPlugin.getPluginState();
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
        Application.getInstance().showLoadingIndicator();
        final PluginPanel formPluginPanel = formPlugin.getOwner();
        final FormPlugin newFormPlugin = ComponentRegistry.instance.get("form.plugin");
        // после обновления формы ей снова "нужно дать" локальную шину событий
        newFormPlugin.setLocalEventBus(this.eventBus);
        final FormPluginConfig newConfig = new FormPluginConfig(event.getId());
        final FormPluginData formPluginData = formPlugin.getInitialData();
        newConfig.setPluginState((FormPluginState) formPluginData.getPluginState());
        newConfig.setFormViewerConfig(getFormViewerConfig());
        newFormPlugin.setConfig(newConfig);
        formPluginPanel.open(newFormPlugin);
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
 /*       formPlugin.setTemporaryWidth(width);
        formPlugin.setTemporaryHeight(height / 2);
        getView().onPluginPanelResize();

        if (formPlugin != null && formPlugin.getView() != null) {
            formPlugin.getView().onPluginPanelResize();
        }*/
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
                ApplicationWindow.errorAlert(caught.getMessage());
            }
            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = getCollectionPlugin().getNavigationConfig();
                LinkUtil.addHierarchicalLinkToNavigationConfig(navigationConfig, link);
                Application.getInstance().getEventBus().fireEvent(
                        new NavigationTreeItemSelectedEvent(pluginConfig, link.getName(), navigationConfig));
            }
        });
    }

    private class FormPluginCreatedListener implements PluginViewCreatedEventListener {

        @Override
        public void onViewCreation(PluginViewCreatedEvent source) {
            Application.getInstance().hideLoadingIndicator();
            ((DomainObjectSurferPluginData) getInitialData())
                    .setFormPluginData((FormPluginData) source.getPlugin().getInitialData());
            getView().updateActionToolBar();
        }
    }
}
