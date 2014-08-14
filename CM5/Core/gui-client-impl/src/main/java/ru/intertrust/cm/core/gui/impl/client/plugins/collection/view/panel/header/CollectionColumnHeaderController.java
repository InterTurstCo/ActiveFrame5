package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentOrderChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentOrderChangedHandler;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentWidthChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentWidthChangedHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionParameterizedColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnHiddenActionContext;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 21/03/14
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderController implements ComponentWidthChangedHandler, ComponentOrderChangedHandler {
    private final String collectionViewName;
    private List<ColumnHeaderBlock> columnHeaderBlocks;
    private CollectionDataGrid dataGrid;
    private ColumnSelectorPopup popup;
    private int displayedWidth;

    public CollectionColumnHeaderController(final String collectionViewName,
                                            final CollectionDataGrid dataGrid, int displayedWidth, EventBus eventBus) {
        this.collectionViewName = collectionViewName;
        this.dataGrid = dataGrid;
        this.displayedWidth = displayedWidth;
        eventBus.addHandler(ComponentWidthChangedEvent.TYPE, this);
        eventBus.addHandler(ComponentOrderChangedEvent.TYPE, this);
    }

    public void setDisplayedWidth(int displayedWidth) {
        this.displayedWidth = displayedWidth;
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

    public void changeVisibilityOfColumns() {

        for (int i = 0; i < columnHeaderBlocks.size(); i++) {
            ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
            CollectionColumn collectionColumn = columnHeaderBlock.getColumn();
            boolean visible = collectionColumn.isVisible();
            boolean shouldChangeVisibilityState = columnHeaderBlock.shouldChangeVisibilityState();

            if (visible && shouldChangeVisibilityState) {
                dataGrid.insertColumn(i, collectionColumn, columnHeaderBlock.getHeader());
                dataGrid.setColumnWidth(collectionColumn, collectionColumn.getDrawWidth() + "px");
                columnHeaderBlock.setShouldChangeVisibilityState(false);
                collectionColumn.setVisible(true);


            }
            if (!visible && shouldChangeVisibilityState) {
                columnHeaderBlock.getHeader().saveFilterValue();
                dataGrid.removeColumn(collectionColumn);
                columnHeaderBlock.setShouldChangeVisibilityState(false);
                collectionColumn.setVisible(false);

            }
        }

        CollectionDataGridUtils.adjustColumnsWidth(Math.max(dataGrid.getOffsetWidth(), displayedWidth), dataGrid);
        dataGrid.redraw();


    }

    private void storeUserSettings() {
        final ActionConfig actionConfig = new ActionConfig();
        actionConfig.setImmediate(true);
        actionConfig.setDirtySensitivity(false);
        final CollectionColumnHiddenActionContext actionContext = new CollectionColumnHiddenActionContext();
        actionContext.setActionConfig(actionConfig);
        actionContext.setLink(Application.getInstance().getHistoryManager().getLink());
        actionContext.setCollectionViewName(collectionViewName);
        for (int index = 0; index < columnHeaderBlocks.size(); index++) {
            final CollectionColumn column = columnHeaderBlocks.get(index).getColumn();
            if (!column.isVisible()) {
                actionContext.putHidden(column.getFieldName());
            }
        }
        final Action action = ComponentRegistry.instance.get(CollectionColumnHiddenActionContext.COMPONENT_NAME);
        action.setInitialContext(actionContext);
        action.perform();
    }


    @Override
    public void handleEvent(ComponentWidthChangedEvent event) {
        Object component = event.getComponent();
        if (!(component instanceof CollectionColumn)) {
            return;
        }
        CollectionColumn column = (CollectionColumn) component;

        int oldWidth = column.getDrawWidth();
        int newWidth = event.getWidth();
        dataGrid.setColumnWidth(column, newWidth + "px");
        column.setDrawWidth(newWidth);
        column.setUserWidth(newWidth);
        if (oldWidth > newWidth) {
            int index = dataGrid.getColumnIndex(column);
            changeRelativeRightColumnWidth(index, oldWidth - newWidth);
        }
        saveFilterValues();
        dataGrid.redraw();
        updateFilterValues();
    }

    private void changeRelativeRightColumnWidth(int index, int delta) {
        CollectionColumn relativeRightColumn = findNextVisibleRightColumn(index);
        if (relativeRightColumn == null) {
            return;
        }
        int newDrawWidth = relativeRightColumn.getDrawWidth() + delta;
        dataGrid.setColumnWidth(relativeRightColumn, newDrawWidth + "px");
        relativeRightColumn.setDrawWidth(newDrawWidth);
        relativeRightColumn.setUserWidth(newDrawWidth);

    }

    private CollectionColumn findNextVisibleRightColumn(int index) {
        int columnCount = dataGrid.getColumnCount();
        if (columnCount == index + 1) {
            return null; //column is last
        }
        int size = columnHeaderBlocks.size();
        for (int i = 0; i < size; i++) {
            ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
            CollectionColumn column = columnHeaderBlock.getColumn();
            if (i == index + 1) {
                if (column.isVisible()) {
                    return column;

                } else {
                    int shift = 2;
                    while (index + shift < size) {
                        shift++;
                        CollectionColumn shiftedColumn = columnHeaderBlocks.get(index + shift).getColumn();
                        if (shiftedColumn.isVisible()) {
                            return shiftedColumn;
                        }

                    }
                }

            }

        }
        return null;
    }

    @Override
    public void handleEvent(ComponentOrderChangedEvent event) {
        Object component = event.getComponent();
        if (!(component instanceof CollectionColumn)) {
            return;
        }
        int indexFrom = event.getFromOrder();
        ColumnHeaderBlock columnHeader = columnHeaderBlocks.get(indexFrom);
        int indexTo = event.getToOrder();
        columnHeaderBlocks.remove(columnHeader);
        columnHeaderBlocks.add(indexTo, columnHeader);
        CollectionColumn movedColumn = columnHeader.getColumn();
        if (movedColumn instanceof CollectionParameterizedColumn) {
            saveFilterValues();
            dataGrid.removeColumn(indexFrom);
            dataGrid.insertColumn(indexTo, movedColumn, columnHeader.getHeader());
            updateFilterValues();

        }
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
                    boolean shouldChangeVisibilityPrevious = columnHeaderBlock.shouldChangeVisibilityState();
                    columnHeaderBlock.setShouldChangeVisibilityState(!shouldChangeVisibilityPrevious);
                    column.setVisible(checked);

                }
            });
            container.add(checkBox);

        }
    }

    private class ChangeVisibilityClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            popup.hide();
            changeVisibilityOfColumns();
            storeUserSettings();
        }
    }
}
