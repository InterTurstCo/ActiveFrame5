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
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
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
import ru.intertrust.cm.core.gui.api.client.CustomEdit;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEvent;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEventHandler;
import ru.intertrust.cm.core.gui.api.client.event.CustomEditEvent;
import ru.intertrust.cm.core.gui.api.client.event.CustomEditEventHandler;
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
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        CustomDeleteEventHandler, CustomEditEventHandler, CollectionRemoteManagement, FiltersRemoteManagement, UpdateCollectionEventHandler,
        CollectionViewerUpdatedEventHandler {
    private static int MAGIC_NUMBER = 40;
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
    private Panel pluginWrapper;
    private boolean stretch = false;

    @Override
    public void setCurrentState(WidgetState currentState) {
        state = (TableViewerState) currentState;

        CollectionViewerConfig config = initCollectionConfig(state);
        collectionWidgetHelper.openCollectionPlugin(config, null, pluginPanel, localEventBus);
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
        setStretch(state);
        return initView();
    }

    private void setStretch(WidgetState state){
        TableViewerState sTate = (TableViewerState)state;
        stretch = sTate.getStretched()&&
                (sTate.getTableViewerConfig().getPageSize()==null || sTate.getTableViewerConfig().getPageSize()<=0)
                &&
                (getDisplayConfig()==null || getDisplayConfig().getHeight()==null);
    }

    private CollectionPlugin getCollectionPlugin() {
        return (CollectionPlugin) pluginPanel.getCurrentPlugin();
    }

    private Widget initView() {
        WidgetDisplayConfig displayConfig = getDisplayConfig();
        pluginWrapper = new AbsolutePanel();
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
        localEventBus.addHandler(CustomEditEvent.TYPE, this);
        localEventBus.addHandler(UpdateCollectionEvent.TYPE, this);
        localEventBus.addHandler(CollectionViewerUpdatedEvent.TYPE, this);
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
        resetSelection();
        config = state.getTableViewerConfig();

        toolbar.setProcessesMenuVisible(config.isShowWorkflowMenu());
        toolbar.setActionsMenuVisible(config.isShowActionsMenu());

        TableBrowserParams tableBrowserParams = createTableBrowserParams(config);

        if ((config.getCollectionViewerConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig().isUseDefault()) &&
                (editableState ||
                        (config.getIgnoreFormReadOnlyStateConfig() != null && config.getIgnoreFormReadOnlyStateConfig().isValue()))) {
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

        if (config.getEditComponent() != null) {
            CustomEdit editComponent = ComponentRegistry.instance.get(config.getEditComponent());
            if (editComponent != null) {
                editComponent.edit(event.getId(), this, localEventBus);
            } else {
                Window.alert("Не найден компонент для редактирования: " + config.getEditComponent());
            }

        } else {
            if (getLinkedFormMappingConfig() != null && (editableState ||
                    (config.getIgnoreFormReadOnlyStateConfig() != null && config.getIgnoreFormReadOnlyStateConfig().isValue()))) {
                HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(event.getId(), null,
                        localEventBus, false, null, this, false).withModalWindow(true);
                clickHandler.processClick();
            }
        }
    }

    @Override
    public CollectionExtraFiltersConfig getCollectionExtraFilters() {
        CollectionPlugin coPlugin = getCollectionPlugin();
        if (((CollectionPluginData) coPlugin.getInitialData()).getTableBrowserParams() != null) {
            return ((CollectionPluginData) coPlugin.getInitialData()).getTableBrowserParams().getCollectionExtraFiltersConfig();
        } else
            return null;
    }

    @Override
    public void applyCollectionExtraFilters(CollectionExtraFiltersConfig config) {
        CollectionPlugin coPlugin = getCollectionPlugin();
        ((CollectionPluginData) coPlugin.getInitialData()).getTableBrowserParams().setCollectionExtraFiltersConfig(config);
        refresh();
    }

    @Override
    public void resetColumnFilters() {
        CollectionPlugin coPlugin = getCollectionPlugin();
        ((CollectionPluginView) coPlugin.getView()).getEventBus().fireEvent(new FilterEvent(true));
    }

    @Override
    public Map<String, List<String>> getColumnFiltersMap() {
        CollectionPlugin coPlugin = getCollectionPlugin();
        CollectionPluginView copV = (CollectionPluginView) coPlugin.getView();
        return copV.getFiltersMap();
    }

    @Override
    public void applyColumnFilters(Map<String, List<String>> filtersMap) {
        CollectionPlugin coPlugin = getCollectionPlugin();
        CollectionPluginView copV = (CollectionPluginView) coPlugin.getView();
        copV.setFiltersMap(filtersMap);
        ((CollectionPluginView) coPlugin.getView()).getEventBus().fireEvent(new FilterEvent(false, true));
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
        if (selectedIds.size() <= 0) {
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
                        if (state.getRootObject() != null && state.getRootObject().getId() != null) {
                            SelectDomainObjectTypePopup selectDomainObjectTypePopup = new SelectDomainObjectTypePopup(createdObjectsConfig);
                            selectDomainObjectTypePopup.show();
                        } else {
                            Window.alert("Корневой обьект еще не сохранен.");
                        }
                    }
                });
            }
        }
        return null;
    }

    @Override
    public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
        if (toolbar.getToolbarPanel().isVisible()) {
            if (!event.isDeselected()) {
                selectedIds.add(event.getId());
                toolbar.setSelectedIds(selectedIds);
            } else {
                selectedIds.remove(event.getId());
                toolbar.setSelectedIds(selectedIds);
            }
            toolbar.activateSingleRowAction(false, false);
        }
        toolbar.setSelectedId(null);
    }

    @Override
    public void onDelete(CustomDeleteEvent event) {
        if (event.getStatus().equals(DeleteStatus.ERROR)) {
            Window.alert("Ошибка удаления обьекта : " + event.getMessage());
        } else {
            CollectionPlugin coPlugin = getCollectionPlugin();
            CollectionPluginView cpView = (CollectionPluginView) coPlugin.getView();
            cpView.delCollectionRow(event.getDeletedObject());
            if (event.isRefreshRequired() || config.isRefreshAllOnAction()) {
                refresh();
            }
            refreshSelection();
        }
    }

    private void refresh() {
        CollectionPlugin coPlugin = getCollectionPlugin();
        coPlugin.refresh();
    }

    /**
     * Обновляет Id выделенного элемента.<br/>
     * В настоящий момент необходимо для установления правильного Id после удаления элемента из списка<br/>
     * и последующей автоматической смены выделенного элемента.
     */
    private void refreshSelection() {
        CollectionPlugin coPlugin = getCollectionPlugin();
        CollectionPluginView cpView = (CollectionPluginView) coPlugin.getView();

        List<Id> selectedIdsList = cpView.getSelectedIds();

        if (selectedIdsList.size() > 0) {
            selectedId = selectedIdsList.get(0);
        } else {
            selectedId = null;
        }
        toolbar.setSelectedId(selectedId);
    }

    /**
     * Сбрасывает все локальные переменные выделенных элементов.<br/>
     * Необходимо когда на UI сбрасывается состояние со снятием всех чекбоксов/выделенных строк и тп.
     */
    private void resetSelection() {
        selectedId = null;
        toolbar.setSelectedId(selectedId);

        selectedIds.clear();
    }

    @Override
    public void onEdit(CustomEditEvent event) {
        refresh();
    }

    /**
     * Выделить строку с нужным Id обьекта.
     * Если строки нет в текущем списке, догрузить список.
     *
     * @param objectId
     * @return false - если строка не найдена иначе true
     */
    @Override
    public void selectRowById(Id objectId) {
        CollectionPlugin coPlugin = getCollectionPlugin();
        CollectionPluginView cpView = (CollectionPluginView) coPlugin.getView();
        cpView.setSelectedRow(objectId);
    }

    @Override
    public void updateCollection(UpdateCollectionEvent event) {
        if (config.isRefreshAllOnAction()) {

            // TODO: найти способ узнать происходит ли обновление когда корневой объект сохранен, добавлены новые дочерные в виджет, но еще не сохранены. В этом случае не делать обновление.
            IdentifiableObject identifiableObject = event.getIdentifiableObject();
            if (identifiableObject != null) {
                Date createdDate = identifiableObject.getTimestamp("created_date");
                Date updatedDate = identifiableObject.getTimestamp("updated_date");

                String createdDateStr = createdDate.toString();
                String updatedDateStr = updatedDate.toString();

                boolean isNewObject = createdDateStr.equals(updatedDateStr);
                // если добавляется новый объект, то обновлять остальные нет надобности
                if (!isNewObject) {
                    refresh();
                }
            } else {
                refresh();
            }
        }
        refreshSelection();
    }

    @Override
    public void collectionViewerUpdated(CollectionViewerUpdatedEvent event) {
       if(stretch){
            int rOws = (event.getRows() >= 6) ? event.getRows() : 6;
            pluginWrapper.setHeight(String.valueOf(rOws * MAGIC_NUMBER) + "px");
        }
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
            if (state.getRootObject() != null && state.getRootObject().getId() != null) {
                showNewForm(domainObjectType);
                if (sourcePopup != null) {
                    sourcePopup.hide();
                }
            } else {
                Window.alert("Корневой обьект еще не сохранен.");
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
