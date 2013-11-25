package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.tablebrowser.TableBrowserView;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends BaseWidget {
    private TableBrowserConfig tableBrowserConfig;
    private String collectionName;

    @Override
    public void setCurrentState(WidgetState currentState) {
        TableBrowserState tableBrowserState = (TableBrowserState) currentState;
        collectionName = tableBrowserState.getCollectionName();
        tableBrowserConfig = tableBrowserState.getTableBrowserConfig();
        String widthString = displayConfig.getWidth();
        int width = Integer.parseInt(widthString.replaceAll("\\D+", ""));
        ArrayList<TableBrowserRowItem> items = tableBrowserState.getSelectedItems();
        LinkedHashMap<String, String> map = tableBrowserState.getDomainFieldOnColumnNameMap();

        TableBrowserView view = (TableBrowserView) impl;
        view.setSingleChoice(false);
        view.setWidgetWidth(width);
        view.setSelectedItems(items);
        view.setDomainObjectFieldOnColumnNameMap(map);
        view.buildTable();
        view.cleanUpSelectedRows();
        view.drawSelectedRows();

    }

    @Override
    public TableBrowserState getCurrentState() {
        TableBrowserState state = new TableBrowserState();
        TableBrowserView view = (TableBrowserView) impl;
        state.setSelectedItems(view.getSelectedItems());

        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        TableBrowserView tableBrowserView = new TableBrowserView();
        tableBrowserView.getOpenDialogButton().addClickHandler(new FetchFilteredRowsClickHandler());

        return tableBrowserView;

    }

    private void fetchFilteredRows(String text) {

        FilteredRowsRequest filteredRowsRequest = new FilteredRowsRequest();
        String name = tableBrowserConfig.getCollectionRefConfig().getName();
        filteredRowsRequest.setCollectionName(name);
        TableBrowserView view = (TableBrowserView) impl;
        TableBrowserState tableBrowserState = getCurrentState();
        filteredRowsRequest.setColumnFields(view.getDomainObjectFieldOnColumnNameMap());
        filteredRowsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
        filteredRowsRequest.setText(text);
        filteredRowsRequest.setExcludeIds(tableBrowserState.getIds());
        filteredRowsRequest.setInputTextFilterName(tableBrowserConfig.getInputTextFilterConfig().getName());
        filteredRowsRequest.setIdsExclusionFilterName(tableBrowserConfig.getSelectionExcludeFilterConfig().getName());

        Command command = new Command("fetchFilteredRows", getName(), filteredRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                FilteredRowsList list = (FilteredRowsList) result;

                List<TableBrowserRowItem> items = list.getFilteredRows();
                TableBrowserView view = (TableBrowserView) impl;
                view.setTableData(items);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    @Override
    public Component createNew() {
        return new TableBrowserWidget();
    }


    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            TableBrowserView view = (TableBrowserView) impl;
            String text = view.getFilterEditor().getValue();
            fetchFilteredRows(text);
            DialogBox dialogBox = view.getDialogBox();
            dialogBox.center();
            dialogBox.show();

        }
    }

}