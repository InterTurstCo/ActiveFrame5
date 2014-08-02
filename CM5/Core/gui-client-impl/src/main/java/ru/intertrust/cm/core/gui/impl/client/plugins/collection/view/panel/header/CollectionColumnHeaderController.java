package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.impl.client.history.UserSettingsObject;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.ColumnSettingsObject;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 21/03/14
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderController {
    private List<ColumnHeaderBlock> columnHeaderBlocks;
    private CollectionDataGrid dataGrid;
    private String collectionIdentifier;
    private ColumnSelectorPopup popup;

    public CollectionColumnHeaderController(String collectionIdentifier, CollectionDataGrid dataGrid) {
        this.dataGrid = dataGrid;
        this.collectionIdentifier = collectionIdentifier;

    }

    public List<ColumnHeaderBlock> getColumnHeaderBlocks() {
        return columnHeaderBlocks;
    }

    public void setColumnHeaderBlocks(List<ColumnHeaderBlock> columnHeaderBlocks) {
        this.columnHeaderBlocks = columnHeaderBlocks;
    }

    public void changeFiltersInputsVisibility(boolean showFilter) {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            header.setSearchAreaVisibility(showFilter);


        }

    }

    public void clearFilters() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();

            header.hideClearButton();
            header.resetFilterValue();

        }

    }

    public void updateFilterValues() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            header.updateFilterValue();

        }
    }

    public void saveFilterValues() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            header.saveFilterValue();

        }

    }

    public void setFocus() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            header.setFocus();

        }
    }

    public void showPopup(UIObject target) {
        if (popup == null) {
            popup = new ColumnSelectorPopup();
        }
        popup.showRelativeTo(target);
    }


    private class ColumnSelectorPopup extends PopupPanel {
        private ColumnSelectorPopup() {
            super(true);
            initContent();
        }


        private void initContent() {
            this.setStyleName("columnSettingPopupPanel");
            Panel header = new AbsolutePanel();
            Panel body = new AbsolutePanel();
            initCheckBoxItems(body);
            Button submit = new Button("Ok");
            submit.setStyleName("dark-button");
            submit.addClickHandler(new ChangeVisibilityClickHandler());
            body.add(submit);
            Panel container = new AbsolutePanel();
            container.setStyleName("settings-popup columnSettingsPopup");
            container.add(header);
            container.add(body);
            this.add(container);
        }

        private void initCheckBoxItems(Panel container) {
            for (int i = 0; i < columnHeaderBlocks.size(); i++) {
                ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
                initCheckboxItem(columnHeaderBlock, container);

            }
        }

        private void initCheckboxItem(final ColumnHeaderBlock columnHeaderBlock, Panel container) {
            final CollectionColumn column = columnHeaderBlock.getColumn();
            CheckBox checkBox = new CheckBox(column.getDataStoreName());
            checkBox.setValue(column.isVisible());
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean checked = event.getValue();
                    columnHeaderBlock.setShouldChangeVisibilityState(true);
                    column.setVisible(checked);

                }
            });
            container.add(checkBox);

        }
    }

    private class ChangeVisibilityClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {

            for (int i = 0; i < columnHeaderBlocks.size(); i++) {

                popup.hide();
                handleHistory();
                changeVisibilityOfColumns();
            }
        }
    }

    public void changeVisibilityOfColumns() {
        boolean shouldBeRedrawn = false;
        for (int i = 0; i < columnHeaderBlocks.size(); i++) {
            ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
            CollectionColumn collectionColumn = columnHeaderBlock.getColumn();
            boolean visible = collectionColumn.isVisible();
            boolean shouldChangeVisibilityState = columnHeaderBlock.shouldChangeVisibilityState();

            if (visible && shouldChangeVisibilityState) {
                dataGrid.setColumnWidth(collectionColumn, collectionColumn.getCalculatedWidth(), Style.Unit.PX);
                columnHeaderBlock.setShouldChangeVisibilityState(false);
                collectionColumn.setVisible(true);
                shouldBeRedrawn = true;

            }
            if (!visible && shouldChangeVisibilityState) {
                dataGrid.setColumnWidth(collectionColumn, 0, Style.Unit.PX);
                columnHeaderBlock.setShouldChangeVisibilityState(true);
                collectionColumn.setVisible(false);
                shouldBeRedrawn = true;
            }
        }
        if (shouldBeRedrawn) {
            dataGrid.redraw();
        }
    }

    public void handleHistory() {

        final UserSettingsObject userSettingsObject = UserSettingsUtil.getUserSettingsObjectForColumns(collectionIdentifier);
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            String field = columnHeaderBlock.getHeader().getHeaderWidget().getFieldName();
            final ColumnSettingsObject columnSettingsObject = UserSettingsUtil.getColumnSettingsObject(userSettingsObject, field);
            columnSettingsObject.setVisible(columnHeaderBlock.getColumn().isVisible());
        }

        final HistoryItem item = new HistoryItem(HistoryItem.Type.USER_INTERFACE,
                UserSettingsHelper.COLUMN_SETTINGS_KEY, new JSONObject(userSettingsObject).toString());
        Application.getInstance().getHistoryManager().addHistoryItems(collectionIdentifier, item);


    }


}
