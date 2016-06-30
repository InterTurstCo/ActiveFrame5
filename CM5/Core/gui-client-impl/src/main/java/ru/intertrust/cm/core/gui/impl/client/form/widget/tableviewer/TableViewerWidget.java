package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEvent;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEventHandler;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.breadcrumb.CollectionWidgetHelper;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.DialogBoxAction;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable.LinkedFormDialogBoxBuilder;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DEFAULT_EMBEDDED_COLLECTION_TABLE_HEIGHT;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DEFAULT_EMBEDDED_COLLECTION_TABLE_WIDTH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:34
 */
@ComponentName("table-viewer")
public class TableViewerWidget extends BaseWidget implements ParentTabSelectedEventHandler, HierarchicalCollectionEventHandler,
        OpenDomainObjectFormEventHandler, HasLinkedFormMappings, CollectionRowSelectedEventHandler, BreadCrumbNavigationEventHandler, CheckBoxFieldUpdateEventHandler,
        CustomDeleteEventHandler, CollectionRemoteManagement {

    private PluginPanel pluginPanel;
    private EventBus localEventBus;
    private CollectionWidgetHelper collectionWidgetHelper;
    private TableViewerConfig config;
    private TableViewerToobar toolbar;
    private Id selectedId;
    private HandlerRegistration addButtonHandlerRegistration;
    private TableViewerState state;
    private Boolean editableState = true;
    private List<Id> selectedIds;

    @Override
    public void setCurrentState(WidgetState currentState) {
        state = (TableViewerState) currentState;
        CollectionViewerConfig config = initCollectionConfig(state);
        collectionWidgetHelper.openCollectionPlugin(config, null, pluginPanel);
        toolbar.setConfig(this.config);
        if (toolbar.getAddButton() != null && state.getTableViewerConfig().getLinkedFormMappingConfig() != null &&
                state.getTableViewerConfig().getCreatedObjectsConfig() != null) {
            if (state.hasAllowedCreationDoTypes()) {
                if (addButtonHandlerRegistration != null) {
                    addButtonHandlerRegistration.removeHandler();
                }
                addButtonHandlerRegistration = addHandlersToAddButton(toolbar.getAddButton());

            } else {
                toolbar.getAddButton().removeFromParent();
            }
        }
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return new TableViewerState();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return initView();

    }

    private Widget initView() {
        WidgetDisplayConfig displayConfig = getDisplayConfig();
        final Panel pluginWrapper = new AbsolutePanel();
        pluginWrapper.addStyleName("table-viewer-wrapper");
        pluginPanel = new PluginPanel();
        localEventBus = new SimpleEventBus();

        selectedIds = new ArrayList<>();
        toolbar = new TableViewerToobar(localEventBus, selectedIds);

        pluginWrapper.add(toolbar.getToolbarPanel());

        String height = displayConfig.getHeight() == null ? DEFAULT_EMBEDDED_COLLECTION_TABLE_HEIGHT : displayConfig.getHeight();
        pluginWrapper.setHeight(height);
        String width = displayConfig.getWidth() == null ? DEFAULT_EMBEDDED_COLLECTION_TABLE_WIDTH : displayConfig.getWidth();
        pluginWrapper.setWidth(width);
        int tableWidth = Integer.parseInt(width.replaceAll("\\D+", ""));
        pluginPanel.setVisibleWidth(tableWidth);
        pluginWrapper.add(pluginPanel);
        eventBus.addHandler(ParentTabSelectedEvent.TYPE, this);
        collectionWidgetHelper = new CollectionWidgetHelper(localEventBus);
        localEventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);
        localEventBus.addHandler(OpenDomainObjectFormEvent.TYPE, this);
        localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, this);
        localEventBus.addHandler(BreadCrumbNavigationEvent.TYPE, this);
        localEventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, this);
        localEventBus.addHandler(CustomDeleteEvent.TYPE, this);
        localEventBus.addHandler(UpdateCollectionEvent.TYPE, new UpdateCollectionEventHandler() {
            @Override
            public void updateCollection(UpdateCollectionEvent event) {

            }
        });
        WidgetsContainer wc = getContainer();
        if (wc.getPlugin() instanceof FormPlugin) {
            editableState = ((FormPlugin) wc.getPlugin()).getInitialVisualState().isEditable();
        }
        return pluginWrapper;
    }


    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public Component createNew() {
        return new TableViewerWidget();
    }

    private CollectionViewerConfig initCollectionConfig(TableViewerState state) {
        selectedId = null;
        config = state.getTableViewerConfig();
        TableBrowserParams tableBrowserParams = createTableBrowserParams(config);

        if ((config.getCollectionViewerConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig().isUseDefault()) &&
                (editableState ||
                        (config.getIgnoreFormReadOnlyStateConfig()!=null && config.getIgnoreFormReadOnlyStateConfig().isValue()))) {
            toolbar.getToolbarPanel().setVisible(true);
        } else {
            toolbar.getToolbarPanel().setVisible(false);
        }

        if (config.getCollectionViewerConfig() == null) {
            CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
            CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
            collectionViewerConfig.setTableBrowserParams(tableBrowserParams);
            collectionViewRefConfig.setName(config.getCollectionViewRefConfig().getName());
            CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
            collectionRefConfig.setName(config.getCollectionRefConfig().getName());
            DefaultSortCriteriaConfig defaultSortCriteriaConfig = config.getDefaultSortCriteriaConfig();
            collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
            collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
            collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
            collectionViewerConfig.setEmbedded(true);
            return collectionViewerConfig;
        } else {
            config.getCollectionViewerConfig().setTableBrowserParams(tableBrowserParams);
            config.getCollectionViewerConfig().setEmbedded(true);
            return config.getCollectionViewerConfig();
        }
    }

    private TableBrowserParams createTableBrowserParams(TableViewerConfig config) {
        ComplexFiltersParams filtersParams = GuiUtil.createComplexFiltersParams(getContainer());


        TableBrowserParams tableBrowserParams = new TableBrowserParams()
                .setComplexFiltersParams(filtersParams)
                .setIds(new ArrayList<Id>())
                .setDisplayOnlySelectedIds(false)
                .setDisplayCheckBoxes(false)
                .setDisplayChosenValues(true)
                .setPageSize(config.getPageSize())
                .setCollectionExtraFiltersConfig((config.getCollectionViewerConfig() != null) ? config.getCollectionViewerConfig().getCollectionExtraFiltersConfig() :
                        config.getCollectionExtraFiltersConfig())
                .setHasColumnButtons(config.getCollectionTableButtonsConfig() == null ? true
                        : config.getCollectionTableButtonsConfig().isDisplayAllPossible());
        return tableBrowserParams;
    }

    @Override
    public void onParentTabSelectedEvent(ParentTabSelectedEvent event) {
        Element parentElement = event.getParent().getElement();
        Node widgetNode = pluginPanel.asWidget().getElement().getParentNode();
        boolean widgetIsChildOfSelectedTab = parentElement.isOrHasChild(widgetNode);
        boolean viewIsInitialized = pluginPanel.getCurrentPlugin() != null && pluginPanel.getCurrentPlugin().getView() != null;
        if (widgetIsChildOfSelectedTab && viewIsInitialized) {
            pluginPanel.getCurrentPlugin().getView().onPluginPanelResize();
        }
    }

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
        CollectionViewerConfig config = initCollectionConfig(this.<TableViewerState>getInitialData());
        collectionWidgetHelper.handleHierarchyEvent(event, config, pluginPanel);
        selectedId = null;
        toolbar.setSelectedId(selectedId);
    }

    @Override
    public void onOpenDomainObjectFormEvent(OpenDomainObjectFormEvent event) {

        if (getLinkedFormMappingConfig() != null && (editableState ||
                (config.getIgnoreFormReadOnlyStateConfig()!=null && config.getIgnoreFormReadOnlyStateConfig().isValue()))) {
            HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(event.getId(), null,
                    localEventBus, false, null, this, false).withModalWindow(true);
            clickHandler.processClick();
        }

    }


    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return config.getLinkedFormMappingConfig();
    }

    @Override
    public LinkedFormConfig getLinkedFormConfig() {
        return null;
    }


    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        if (selectedIds.size()<=0) {
            selectedId = event.getId();
            toolbar.setSelectedId(selectedId);
        }

    }

    @Override
    public void onNavigation(BreadCrumbNavigationEvent event) {
        selectedId = null;
        toolbar.setSelectedId(selectedId);
    }

    private HandlerRegistration addHandlersToAddButton(Button button) {
        final CreatedObjectsConfig createdObjectsConfig = state.getRestrictedCreatedObjectsConfig();
        if (createdObjectsConfig != null && !createdObjectsConfig.getCreateObjectConfigs().isEmpty()) {
            if (createdObjectsConfig.getCreateObjectConfigs().size() == 1) {
                String domainObjectType = createdObjectsConfig.getCreateObjectConfigs().get(0).getDomainObjectType();
                return button.addClickHandler(new OpenFormClickHandler(domainObjectType, null));
            } else {
                return button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        SelectDomainObjectTypePopup selectDomainObjectTypePopup = new SelectDomainObjectTypePopup(createdObjectsConfig);
                        selectDomainObjectTypePopup.show();
                    }
                });
            }
        }
        return null;
    }

    @Override
    public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
        if(toolbar.getToolbarPanel().isVisible()) {
            if (!event.isDeselected()) {
                selectedIds.add(event.getId());
                toolbar.setSelectedIds(selectedIds);
            } else {
                selectedIds.remove(event.getId());
                toolbar.setSelectedIds(selectedIds);
            }
            toolbar.deactivateSingleRowAction();
        }
        toolbar.setSelectedId(null);
    }

    @Override
    public void onDelete(CustomDeleteEvent event) {
        if(event.getStatus().equals(DeleteStatus.ERROR)){
            Window.alert("Ошибка удаления обьекта : " + event.getMessage());
        } else {
            CollectionPlugin coPlugin = (CollectionPlugin)pluginPanel.getCurrentPlugin();
            CollectionPluginView cpView = (CollectionPluginView)coPlugin.getView();
            cpView.delCollectionRow(event.getDeletedObject());
            if(event.isRefreshRequired()){
                coPlugin.refresh();
            }
        }

    }

    /**
     * Выделить строку с нужным Id обьекта.
     * Если строки нет в текущем списке, догрузить список.
     * @param objectId
     * @return false - если строка не найдена иначе true
     */
    @Override
    public void SelectRowById(Id objectId) {
        CollectionPlugin coPlugin = (CollectionPlugin)pluginPanel.getCurrentPlugin();
        CollectionPluginView cpView = (CollectionPluginView)coPlugin.getView();
        cpView.setSelectedRow(objectId);
    }


    class OpenFormClickHandler implements ClickHandler {
        private String domainObjectType;
        private PopupPanel sourcePopup;

        OpenFormClickHandler(String domainObjectType, PopupPanel sourcePopup) {
            this.domainObjectType = domainObjectType;
            this.sourcePopup = sourcePopup;
        }

        @Override
        public void onClick(ClickEvent event) {
            showNewForm(domainObjectType);
            if (sourcePopup != null) {
                sourcePopup.hide();
            }
        }
    }

    class SelectDomainObjectTypePopup extends PopupPanel {
        SelectDomainObjectTypePopup(CreatedObjectsConfig createdObjectsConfig) {
            super(true, false);
            this.setPopupPosition(toolbar.getAddButton().getAbsoluteLeft() - 48, toolbar.getAddButton().getAbsoluteTop() + 40);
            AbsolutePanel header = new AbsolutePanel();
            header.setStyleName("srch-corner");
            final VerticalPanel body = new VerticalPanel();
            AbsolutePanel container = new AbsolutePanel();
            container.setStyleName("settings-popup");
            container.getElement().getStyle().clearOverflow();

            for (CreatedObjectConfig createdObjectConfig : createdObjectsConfig.getCreateObjectConfigs()) {
                final AbsolutePanel menuItemContainer = new AbsolutePanel();
                menuItemContainer.setStyleName("settingsItem");
                menuItemContainer.add(new Label(createdObjectConfig.getText()));
                menuItemContainer.addDomHandler(new OpenFormClickHandler(createdObjectConfig.getDomainObjectType(), this), ClickEvent.getType());
                body.add(menuItemContainer);
            }
            container.add(header);
            container.add(body);
            this.add(container);
        }
    }


    protected void showNewForm(final String domainObjectType) {
        LinkedFormDialogBoxBuilder linkedFormDialogBoxBuilder = new LinkedFormDialogBoxBuilder();

        DialogBoxAction saveAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                SaveAction action = getSaveAction(formPlugin, null);
                action.perform();
            }
        };


        DialogBoxAction cancelAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                // no op
            }
        };
        LinkedFormDialogBoxBuilder lfb = linkedFormDialogBoxBuilder
                .setSaveAction(saveAction)
                .setCancelAction(cancelAction)
                .withHeight(GuiUtil.getModalHeight(domainObjectType, state.getTableViewerConfig().getLinkedFormMappingConfig(), null))
                .withWidth(GuiUtil.getModalWidth(domainObjectType, state.getTableViewerConfig().getLinkedFormMappingConfig(), null))
                .withObjectType(domainObjectType)
                .withLinkedFormMapping(state.getTableViewerConfig().getLinkedFormMappingConfig())
                .withPopupTitlesHolder(null)
                .withParentWidgetIds(null)
                .withWidgetsContainer(getContainer())
                .withTypeTitleMap(null)
                .withFormResizable(false)
                .buildDialogBox();
        lfb.display();

    }

    protected SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        //saveActionContext.setRootObjectId(rootObjectId);
        formPlugin.setLocalEventBus(localEventBus);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        actionConfig.setDirtySensitivity(false);
        saveActionContext.setActionConfig(actionConfig);
        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }


}
