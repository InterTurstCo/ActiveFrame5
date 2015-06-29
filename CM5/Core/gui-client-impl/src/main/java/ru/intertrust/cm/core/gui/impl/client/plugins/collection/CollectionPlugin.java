package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionRowStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionRowStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRefreshRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPlugin extends Plugin implements SideBarResizeEventHandler, CollectionRowStateChangedEventHandler {

    // поле для локальной шины событий
    protected EventBus eventBus;
    protected DomainObjectSurferPlugin containingDomainObjectSurferPlugin;
    private Map<Id, ExpandedRowState> rowStates = new HashMap<Id, ExpandedRowState>();

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
        return new CollectionPluginView(this);

    }

    public CollectionRowsRequest getCollectionRowRequest() {
        return ((CollectionPluginView) getView()).createRequest();
    }

    @Override
    public void refresh() {
        final CollectionRowsRequest rowsRequest = getCollectionRowRequest();
        final List<Id> selectedIds = ((CollectionPluginView) getView()).getSelectedIds();
        final Id selectedId = selectedIds == null ? null : selectedIds.get(0);
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
        ((CollectionPluginView) getView()).sideBarFixPositionEvent(event);
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
        final boolean expandInitial = effectedRowItem.getId() != null;
        Id effectedId = expandInitial ? effectedRowItem.getId() : effectedRowItem.getParentId();
        ExpandedRowState rowState = rowStates.get(effectedId);
        final List<CollectionRowItem> items = getPluginData().getItems();
        final CollectionDataGrid tableBody = ((CollectionPluginView) getView()).getTableBody();
        CollectionRowItem.RowType rowType = effectedRowItem.getRowType();
        final boolean filter = CollectionRowItem.RowType.FILTER.equals(rowType);
        if (expanded) {
            if (rowState == null) {
                rowState = new ExpandedRowState();
                rowStates.put(effectedId, rowState);
            } else if(!filter) {
                rowState.moreItems();
            }
            CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest();
            collectionRowsRequest.setCollectionName(getPluginData().getCollectionName());
            collectionRowsRequest.setColumnProperties(getPluginData().getDomainObjectFieldPropertiesMap());

            collectionRowsRequest.setParentId(effectedId);
            collectionRowsRequest.setLimit(rowState.getLimit());
            collectionRowsRequest.setOffset(rowState.getOffset());
            if (effectedRowItem.getFilters() != null) {
                rowState.setFilters(effectedRowItem.getFilters());
            }
            collectionRowsRequest.setFiltersMap(rowState.getFilters());
            Command command = new Command("getChildrenForExpanding", "collection.plugin", collectionRowsRequest);
            final ExpandedRowState finalRowState = rowState;
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining updated row");
                    caught.printStackTrace();
                }

                @Override
                public void onSuccess(Dto result) {
                    CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                    List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
                    if (filter) {
                        items.removeAll(finalRowState.getItems());
                        finalRowState.getItems().clear();
                    }
                    finalRowState.getItems().addAll(collectionRowItems);
                    int index = items.indexOf(effectedRowItem);
                    index = expandInitial ? index + 1 : (filter ? index + 2 : index - 1);
                    items.addAll(index, collectionRowItems);

                    tableBody.setRowData(items);
                    tableBody.redrawRow(index);
                    tableBody.getRowElement(index).addClassName("collectionRowExpanded");
                    tableBody.redraw();

                }
            });
        } else {
            items.removeAll(rowState.getItems());
            rowStates.put(effectedId, null);
            tableBody.setRowData(items);
            tableBody.redraw();
            int index = items.indexOf(effectedRowItem);
            tableBody.getRowElement(index).removeClassName("collectionRowExpanded");
        }

    }

    private CollectionPluginData getPluginData() {
        return getInitialData();
    }

}