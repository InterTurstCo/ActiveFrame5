package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ExpandableObjectConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.CreateNewObjectAction;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRefreshRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPlugin extends Plugin implements SideBarResizeEventHandler, CollectionRowStateChangedEventHandler,
        CollectionRowFilteredEventHandler, CollectionRowMoreItemsEventHandler, CollectionAddGroupEventHandler, CollectionAddElementEventHandler {

    // поле для локальной шины событий
    protected EventBus eventBus;
    protected DomainObjectSurferPlugin containingDomainObjectSurferPlugin;
    private Map<Id, ExpandedRowState> rowStates = new HashMap<Id, ExpandedRowState>();
    private Map<Id, Boolean> changedRowsSelection = new HashMap<Id, Boolean>();

    // установка локальной шины событий плагину
    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public DomainObjectSurferPlugin getContainingDomainObjectSurferPlugin() {
        return containingDomainObjectSurferPlugin;
    }

    public void setContainingDomainObjectSurferPlugin(DomainObjectSurferPlugin containingDomainObjectSurferPlugin) {
        this.containingDomainObjectSurferPlugin = containingDomainObjectSurferPlugin;
    }

    /**
     * получение локальной шины событий плагину.
     *
     * @return
     */
    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

    public CollectionPlugin() {

    }

    @Override
    public PluginView createView() {
        eventBus.addHandler(CollectionRowStateChangedEvent.TYPE, this);
        eventBus.addHandler(CollectionRowFilteredEvent.TYPE, this);
        eventBus.addHandler(CollectionRowMoreItemsEvent.TYPE, this);
        eventBus.addHandler(CollectionAddGroupEvent.TYPE, this);
        eventBus.addHandler(CollectionAddElementEvent.TYPE, this);
        return new CollectionPluginView(this);

    }

    public CollectionRowsRequest getCollectionRowRequest() {
        return ((CollectionPluginView) getView()).createRequest();
    }

    @Override
    public void refresh() {
        final CollectionRowsRequest rowsRequest = getCollectionRowRequest();
        final List<Id> selectedIds = ((CollectionPluginView) getView()).getSelectedIds();
        final Id selectedId = (selectedIds == null || selectedIds.size() == 0) ? null : selectedIds.get(0);
        List<String> expandableTypes = new ArrayList<String>();
        if (((CollectionViewerConfig) getConfig()).getChildCollectionConfig() != null) {

            for (ExpandableObjectConfig expandableObjectConfig : ((CollectionViewerConfig) getConfig()).getChildCollectionConfig().
                    getExpandableObjectsConfig().getExpandableObjects()) {
                expandableTypes.add(expandableObjectConfig.getObjectName());
            }
        }
        rowsRequest.setExpandableTypes(expandableTypes);
        if (getPluginData().getTableBrowserParams() != null && getPluginData().getTableBrowserParams().getCollectionExtraFiltersConfig()!=null) {
            rowsRequest.setHierarchicalFiltersConfig(getPluginData().getTableBrowserParams().getCollectionExtraFiltersConfig());
        }

        final Command command = new Command("refreshCollection", "collection.plugin",
                new CollectionRefreshRequest(rowsRequest, selectedId));
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Dto result) {
                final CollectionRowsResponse response = (CollectionRowsResponse) result;
                Application.getInstance().showLoadingIndicator();
                CollectionPluginView view = ((CollectionPluginView) getView());
                view.clearScrollHandler();
                view.handleCollectionRowsResponse(response.getCollectionRows(), true);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        Application.getInstance().hideLoadingIndicator();
                    }
                });
            }
        });
    }

    @Override
    public CollectionPlugin createNew() {
        return new CollectionPlugin();
    }

    @Override
    public boolean restoreHistory() {
        final CollectionPluginView view = (CollectionPluginView) getView();
        return view.restoreHistory();
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{CollectionRowSelectedEvent.TYPE, SideBarResizeEvent.TYPE};
    }

    @Override
    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
        CollectionPluginView collectionPluginView = (CollectionPluginView) getView();
        if (collectionPluginView != null) {
            collectionPluginView.sideBarFixPositionEvent(event);
        }
    }

    @Override
    public void clearHandlers() {
        super.clearHandlers();
        ((CollectionPluginView) getView()).clearHandlers();
    }

    @Override
    public void onCollectionRowStateChanged(CollectionRowStateChangedEvent event) {
        final CollectionRowItem effectedRowItem = event.getEffectedRowItem();
        boolean expanded = event.isExpanded();
        effectedRowItem.setExpanded(expanded);
        Id effectedId = effectedRowItem.getId();
        final List<CollectionRowItem> items = getPluginData().getItems();
        if (expanded) {
            ExpandedRowState rowState = new ExpandedRowState();
            rowStates.put(effectedId, rowState);
            int index = items.indexOf(effectedRowItem) + 1;
            fetchAndHandleItems(index, effectedRowItem, rowState);
        } else {
            ExpandedRowState rowState = rowStates.get(effectedId);
            removeItems(items, rowState.getItems());
            rowStates.put(effectedId, null);
            CollectionDataGrid tableBody = ((CollectionPluginView) getView()).getTableBody();
            tableBody.setRowData(items);
            tableBody.redraw();
            int index = items.indexOf(effectedRowItem);
            if (index != -1) {
                tableBody.getRowElement(index).removeClassName("collectionRowExpanded");
            }
        }

    }

    @Override
    public void onCollectionRowFilteredEvent(CollectionRowFilteredEvent event) {
        final CollectionRowItem effectedRowItem = event.getEffectedRowItem();
        final ExpandedRowState rowState = rowStates.get(effectedRowItem.getParentId());
        final List<CollectionRowItem> items = getPluginData().getItems();
        rowState.resetOffset();
        int index = items.indexOf(effectedRowItem);
        CommandCallBack commandCallBack = new CommandCallBack() {
            @Override
            public void execute() {
                items.removeAll(rowState.getItems());
                rowState.getItems().clear();
            }
        };
        if (effectedRowItem.getFilters() != null) {
            rowState.setFilters(effectedRowItem.getFilters());
        }
        fetchAndHandleItems(index, effectedRowItem, rowState, commandCallBack);
    }

    @Override
    public void onMoreItems(CollectionRowMoreItemsEvent event) {
        CollectionRowItem effectedRowItem = event.getEffectedRowItem();
        ExpandedRowState rowState = rowStates.get(effectedRowItem.getParentId());
        rowState.moreItems();
        List<CollectionRowItem> items = getPluginData().getItems();
        int index = items.indexOf(effectedRowItem) - 1;
        if (effectedRowItem.getFilters() != null) {
            rowState.setFilters(effectedRowItem.getFilters());
        }
        fetchAndHandleItems(index, effectedRowItem, rowState);
    }

    public Map<Id, Boolean> getChangedRowsState() {
        return changedRowsSelection;
    }

    public Boolean getCheckBoxDefaultState() {
        RowsSelectionConfig rowsSelectionConfig = getPluginData().getRowsSelectionConfig();
        return rowsSelectionConfig == null ? null : RowsSelectionDefaultState.SELECTED.equals(rowsSelectionConfig.getDefaultState());

    }

    private void removeItems(List<CollectionRowItem> items, List<CollectionRowItem> itemsToRemove) {
        for (CollectionRowItem collectionRowItem : itemsToRemove) {
            items.remove(collectionRowItem);
            ExpandedRowState rowState = rowStates.get(collectionRowItem.getId());
            if (rowState != null) {
                removeItems(items, rowState.getItems());
            }
        }
    }

    private CollectionPluginData getPluginData() {
        return getInitialData();
    }

    private void fetchAndHandleItems(int itemIndex, CollectionRowItem effectedItem, ExpandedRowState rowState) {
        fetchAndHandleItems(itemIndex, effectedItem, rowState, null);
    }

    private void fetchAndHandleItems(final int itemIndex, CollectionRowItem effectedItem, final ExpandedRowState rowState, final CommandCallBack commandCallBack) {
        CollectionRowsRequest collectionRowsRequest = createRequest(effectedItem, rowState);
        List<String> expandableTypes = new ArrayList<String>();

        if (((CollectionViewerConfig) getConfig()).getChildCollectionConfig() != null) {

            for (ExpandableObjectConfig expandableObjectConfig : ((CollectionViewerConfig) getConfig()).getChildCollectionConfig().
                    getExpandableObjectsConfig().getExpandableObjects()) {
                expandableTypes.add(expandableObjectConfig.getObjectName());
            }
            if (((CollectionViewerConfig) getConfig()).getChildCollectionConfig().getDefaultSortCriteriaConfig() != null) {
                collectionRowsRequest.setDefaultSortCriteriaConfig(((CollectionViewerConfig) getConfig()).
                        getChildCollectionConfig().getDefaultSortCriteriaConfig());
            }
        }
        collectionRowsRequest.setExpandableTypes(expandableTypes);

        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) getConfig();
        if (collectionViewerConfig.getChildCollectionConfig() != null) {
            collectionRowsRequest.setCollectionName(collectionViewerConfig.getChildCollectionConfig().getName());
            collectionRowsRequest.setHierarchicalFiltersConfig(collectionViewerConfig.getChildCollectionConfig().getCollectionExtraFiltersConfig());
            Command command = new Command("getChildrenForExpanding", "collection.plugin", collectionRowsRequest);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining updated row");
                    caught.printStackTrace();
                }

                @Override
                public void onSuccess(Dto result) {
                    CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                    handleItems(itemIndex, collectionRowsResponse, rowState, commandCallBack);

                }
            });
        } else {
            Window.alert("Дочерняя коллекция не задана в конфигурации.");
        }


    }

    private CollectionRowsRequest createRequest(CollectionRowItem effectedItem, ExpandedRowState rowState) {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest();
        collectionRowsRequest.setCollectionName(getPluginData().getCollectionName());
        collectionRowsRequest.setColumnProperties(getPluginData().getDomainObjectFieldPropertiesMap());
        Id effectedId = effectedItem.getId() == null ? effectedItem.getParentId() : effectedItem.getId();
        collectionRowsRequest.setParentId(effectedId);
        collectionRowsRequest.setLimit(rowState.getLimit());
        collectionRowsRequest.setOffset(rowState.getOffset());
        collectionRowsRequest.setFiltersMap(rowState.getFilters());
        collectionRowsRequest.setCurrentNestingLevel(effectedItem.getNestingLevel());
        return collectionRowsRequest;
    }

    private void handleItems(int itemIndex, CollectionRowsResponse collectionRowsResponse, ExpandedRowState rowState,
                             CommandCallBack commandCallBack) {
        if (commandCallBack != null) {
            commandCallBack.execute();
        }
        List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
        CollectionPluginView view = (CollectionPluginView) getView();
        final CollectionDataGrid tableBody = view.getTableBody();
        List<CollectionRowItem> items = getPluginData().getItems();
        rowState.getItems().addAll(collectionRowItems);
        items.addAll(itemIndex, collectionRowItems);
        tableBody.setRowData(items);
        tableBody.getRowElement(itemIndex).addClassName("collectionRowExpanded");
        tableBody.redraw();
        if (collectionRowItems.isEmpty()) {
            ApplicationWindow.infoAlert(LocalizeUtil.get(LocalizationKeys.NO_ITEMS_FETCHED));
        }

    }

    @Override
    public void onCollectionAddGroup(CollectionAddGroupEvent event) {
        ChildCollectionConfig childCollectionConfig = ((CollectionViewerConfig) getConfig()).getChildCollectionConfig();
        if (childCollectionConfig.getGroupObjectType() != null) {
            createObjectAction(childCollectionConfig.getGroupObjectType(), event.getEffectedRowItem().getId());
        } else {
            Window.alert("Не указан аттрибут group-object-type элемента child-collection");
        }
    }

    @Override
    public void onCollectionAddElement(CollectionAddElementEvent event) {
        ChildCollectionConfig childCollectionConfig = ((CollectionViewerConfig) getConfig()).getChildCollectionConfig();
        if (childCollectionConfig.getElementObjectType() != null) {
            createObjectAction(childCollectionConfig.getElementObjectType(), event.getEffectedRowItem().getId());
        } else {
            Window.alert("Не указан аттрибут element-object-type элемента child-collection");
        }
    }

    private void createObjectAction(String objectType, Id rootObjectId) {
        CreateNewObjectAction action = ComponentRegistry.instance.get("create.new.object.action");
        ActionContext actionContext = new ActionContext();
        actionContext.setRootObjectId(rootObjectId);
        ActionConfig actionConfig = new ActionConfig();
        actionConfig.getProperties().put("create.object.type", objectType);
        actionConfig.setText("collection-row-button-action");
        actionContext.setActionConfig(actionConfig);
        action.setInitialContext(actionContext);
        action.setPlugin(getContainingDomainObjectSurferPlugin());
        action.perform();
    }

    private interface CommandCallBack {
        void execute();
    }
}