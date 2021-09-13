package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.CompactModeState;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.action.system.CollectionColumnWidthAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.CheckBoxInabilityManager;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnHiddenActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnOrderActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnWidthActionContext;

import java.util.*;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_COLUMN_NAME;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.FILTER_CONTAINER_MARGIN;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21/03/14
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderController implements ComponentWidthChangedHandler, ComponentOrderChangedHandler,
        CollectionRowSelectedEventHandler {
    private final String collectionViewName;
    private List<ColumnHeaderBlock> columnHeaderBlocks;
    private CollectionDataGrid dataGrid;
    private ColumnSelectorPopup popup;
    private int displayedWidth;
    private boolean filtersVisibility;
    private List<com.google.web.bindery.event.shared.HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public CollectionColumnHeaderController(final String collectionViewName,
                                            final CollectionDataGrid dataGrid, int displayedWidth, EventBus eventBus) {
        this.collectionViewName = collectionViewName;
        this.dataGrid = dataGrid;
        this.displayedWidth = displayedWidth;
        handlerRegistrations.add(eventBus.addHandler(ComponentWidthChangedEvent.TYPE, this));
        handlerRegistrations.add(eventBus.addHandler(ComponentOrderChangedEvent.TYPE, this));
        handlerRegistrations.add(eventBus.addHandler(CollectionRowSelectedEvent.TYPE, this));
    }


    public List<ColumnHeaderBlock> getColumnHeaderBlocks() {
        return columnHeaderBlocks;
    }

    public void setColumnHeaderBlocks(List<ColumnHeaderBlock> columnHeaderBlocks) {
        this.columnHeaderBlocks = columnHeaderBlocks;

    }

    public void changeFiltersInputsVisibility(boolean showFilter) {
        filtersVisibility = showFilter;
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            if (columnHeaderBlock.getColumn().isVisible()) {
                header.setSearchAreaVisibility(showFilter);
            }
        }

    }

    public void clearFilters() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            if (columnHeaderBlock.getColumn().isVisible()) {
                header.hideClearButton();
                header.resetFilterValue();
            }

        }

    }

    public void updateFilterValues() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            if (columnHeaderBlock.getColumn().isVisible()) {
                header.updateFilterValue();
            }

        }
    }

    public void saveFilterValues() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            if (columnHeaderBlock.getColumn().isVisible()) {
                header.saveFilterValue();
            }

        }

    }

    private void changeFilterInputWidth() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            CollectionColumn column = columnHeaderBlock.getColumn();
            if (column.isVisible()) {
                header.setFilterInputWidth(column.getDrawWidth() - FILTER_CONTAINER_MARGIN);
            }

        }
    }

    public void setFocus() {
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumnHeader header = columnHeaderBlock.getHeader();
            if (columnHeaderBlock.getColumn().isVisible()) {
                header.setFocus();
            }

        }
    }

    public void showPopup(UIObject target) {
        if (popup == null) {
            popup = new ColumnSelectorPopup();
        }
        popup.showRelativeTo(target);
    }

    public void changeVisibilityOfColumns(boolean keepUserSettings) {
        int widthBeforeChanges = dataGrid.getOffsetWidth();
        for (int i = 0; i < columnHeaderBlocks.size(); i++) {
            ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
            CollectionColumn collectionColumn = columnHeaderBlock.getColumn();
            boolean visible = collectionColumn.isVisible();
            boolean shouldChangeVisibilityState = columnHeaderBlock.shouldChangeVisibilityState();

            if (visible && shouldChangeVisibilityState) {
                if (i > dataGrid.getColumnCount()) {
                    dataGrid.addColumn(collectionColumn, columnHeaderBlock.getHeader());
                } else {
                    dataGrid.insertColumn(i, collectionColumn, columnHeaderBlock.getHeader());
                }
                dataGrid.setColumnWidth(collectionColumn, collectionColumn.getDrawWidth() + "px");
                columnHeaderBlock.getHeader().setSearchAreaVisibility(filtersVisibility);
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
        int tableWidth = displayedWidth - getOtherWidgetsDelta();
        if (keepUserSettings) {
            CollectionDataGridUtils.adjustWidthUserSettingsKeeping(Math.max(widthBeforeChanges, tableWidth), dataGrid);
        } else {
            CollectionDataGridUtils.adjustWidthWithoutUserSettingsKeeping(Math.max(widthBeforeChanges, tableWidth), dataGrid);
        }
        changeFilterInputWidth();
        updateFilterValues();
        dataGrid.redraw();

    }

    public void adjustColumnsWidth(int tableWidth, CollectionDataGrid dataGrid) {
        displayedWidth = tableWidth;
        saveFilterValues();
        CollectionDataGridUtils.adjustWidthUserSettingsKeeping(displayedWidth - getOtherWidgetsDelta(), dataGrid);
        changeFilterInputWidth();

        updateFilterValues();
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

        CollectionColumn column = (CollectionColumn) component;

        int oldWidth = column.getDrawWidth();
        int newWidth = event.getWidth();
        int delta = oldWidth - newWidth;
        if (CollectionDataGridUtils.isTableHorizontalScrollNotVisible(dataGrid)) {
            changeRelativeRightColumnWidth(column, delta);

        } else {
            changeLastColumnWidth(delta);
        }
        dataGrid.setColumnWidth(column, newWidth + "px");
        column.setDrawWidth(newWidth);
        column.setUserWidth(newWidth);
        saveFilterValues();
        dataGrid.redraw();
        updateFilterValues();

        updateWidthSettings();
    }

    private void changeRelativeRightColumnWidth(CollectionColumn column, int delta) {
        ColumnHeaderBlock relativeRightColumnHeaderBlock = findNextVisibleRightColumnBlock(column);
        if (relativeRightColumnHeaderBlock == null) {
            return;
        }
        CollectionColumn relativeRightColumn = relativeRightColumnHeaderBlock.getColumn();
        int newDrawWidth = relativeRightColumn.getDrawWidth() + delta;
        int columnMinWidth = relativeRightColumn.getMinWidth();
        int adjustedNewDrawWidth = newDrawWidth >= columnMinWidth ? newDrawWidth : columnMinWidth;
        dataGrid.setColumnWidth(relativeRightColumn, adjustedNewDrawWidth + "px");
        relativeRightColumn.setDrawWidth(adjustedNewDrawWidth);
        relativeRightColumn.setUserWidth(adjustedNewDrawWidth);
        relativeRightColumnHeaderBlock.getHeader().setFilterInputWidth(adjustedNewDrawWidth - FILTER_CONTAINER_MARGIN);

    }

    private ColumnHeaderBlock findNextVisibleRightColumnBlock(CollectionColumn column) {
        Iterator<ColumnHeaderBlock> iterator = columnHeaderBlocks.iterator();
        while (iterator.hasNext()){
            ColumnHeaderBlock columnHeaderBlock = iterator.next();
            CollectionColumn iteratedColumn = columnHeaderBlock.getColumn();
            if(iteratedColumn.equals(column)){
               return findNextVisibleRightColumnBlock(iterator);
            }
        }
        return null;
    }

    private ColumnHeaderBlock findNextVisibleRightColumnBlock(Iterator<ColumnHeaderBlock> iterator){
        while (iterator.hasNext()){
            ColumnHeaderBlock rightSideColumnHeaderBlock = iterator.next();
            CollectionColumn rightSideColumn = rightSideColumnHeaderBlock .getColumn();
            if(rightSideColumn.isVisible()){
                return rightSideColumnHeaderBlock;
            }
        }
        return null;
    }

    private void changeLastColumnWidth(int delta) {
        ColumnHeaderBlock lastColumnHeaderBlock = findLastVisibleColumnBlock();
        if (lastColumnHeaderBlock == null || delta > 0) {
            return;
        }
        displayedWidth += delta;
    }

    private ColumnHeaderBlock findLastVisibleColumnBlock() {
        int lastIndex = columnHeaderBlocks.size() - 1;
        while (lastIndex >= 0) {
            ColumnHeaderBlock lastColumnHeaderBlock = columnHeaderBlocks.get(lastIndex);
            CollectionColumn shiftedColumn = lastColumnHeaderBlock.getColumn();
            if (shiftedColumn.isVisible()) {
                return lastColumnHeaderBlock;
            }
            lastIndex--;

        }

        return null;
    }

    @Override
    public void handleEvent(ComponentOrderChangedEvent event) {
        Column movedColumn = event.getMovedByUser();
        Column evicted = event.getEvicted();
        ColumnHeaderBlock columnHeader = findColumnHeaderBlock(movedColumn);
        if (columnHeader != null) {
            int listIndexTo = findColumnHeaderBlockIndex(evicted);
            columnHeaderBlocks.remove(columnHeader);
            columnHeaderBlocks.add(listIndexTo, columnHeader);
            saveFilterValues();
            int gridIndexTo = dataGrid.getColumnIndex(event.getEvicted());
            dataGrid.removeColumn(movedColumn);
            dataGrid.insertColumn(gridIndexTo, movedColumn, columnHeader.getHeader());
            updateFilterValues();
            updateOrderSettings();
        }
    }

    private ColumnHeaderBlock findColumnHeaderBlock(Column column) {
        ColumnHeaderBlock result = null;
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            if(columnHeaderBlock.getColumn().equals(column)){
                result = columnHeaderBlock;
                break;
            }
        }
        return result;
    }

    private int findColumnHeaderBlockIndex(Column column) {
        int result = 0;
        for (int i = 0; i < columnHeaderBlocks.size(); i++) {
            ColumnHeaderBlock columnHeaderBlock =  columnHeaderBlocks.get(i);
            if(columnHeaderBlock.getColumn().equals(column)){
                result = i;
                break;
            }
        }
        return result;
    }

    public void changeTableWidthByCondition() {
        if (CollectionDataGridUtils.isTableHorizontalScrollNotVisible(dataGrid)) {
            return;
        }
        changeColumnsWidth(displayedWidth - getOtherWidgetsDelta());
    }

    private void changeColumnsWidth(int tableWidth) {
        saveFilterValues();
        CollectionDataGridUtils.adjustWidthWithoutUserSettingsKeeping(tableWidth, dataGrid);
        dataGrid.redraw();
        changeFilterInputWidth();
        updateFilterValues();
        updateWidthSettings();
    }

    private Map<String, String> createFieldWidthMap() {
        Map<String, String> result = new HashMap<String, String>(dataGrid.getColumnCount());
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            CollectionColumn column = columnHeaderBlock.getColumn();
            result.put(column.getFieldName(), dataGrid.getColumnWidth(column));

        }
        return result;
    }

    public void sideBarFixPositionEvent(final SideBarResizeEvent event) {
            changeColumnsWidth(displayedWidth);

    }

    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        updateFilterValues();
    }

    public void clearHandlers() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
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
            submit.setStyleName("darkButton");
            submit.addClickHandler(new ChangeVisibilityClickHandler());
            body.add(submit);
            Panel container = new AbsolutePanel();
            container.setStyleName("settings-popup columnSettingsPopup");
            container.add(header);
            container.add(body);
            this.add(container);
        }

        private void initCheckBoxItems(Panel container) {
            CheckBoxInabilityManager inabilityManager = new CheckBoxInabilityManager();
            for (int i = 0; i < columnHeaderBlocks.size(); i++) {
                ColumnHeaderBlock columnHeaderBlock = columnHeaderBlocks.get(i);
                initCheckboxItem(columnHeaderBlock, container, inabilityManager);

            }
            inabilityManager.handleCheckBoxesInability();
        }

        private void initCheckboxItem(final ColumnHeaderBlock columnHeaderBlock, Panel container,
                                      final CheckBoxInabilityManager inabilityManager) {
            final CollectionColumn column = columnHeaderBlock.getColumn();
            String checkBoxLabel = column.getDataStoreName();
            if (checkBoxLabel.equalsIgnoreCase(CHECK_BOX_COLUMN_NAME)) {
                return;
            }
            CheckBox checkBox = new CheckBox(column.getDataStoreName());
            checkBox.setValue(column.isVisible());
            inabilityManager.addCheckBox(checkBox);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean checked = event.getValue();
                    boolean shouldChangeVisibilityPrevious = columnHeaderBlock.shouldChangeVisibilityState();
                    columnHeaderBlock.setShouldChangeVisibilityState(!shouldChangeVisibilityPrevious);
                    column.setVisible(checked);
                    inabilityManager.handleCheckBoxesInability();

                }
            });
            container.add(checkBox);

        }
    }

    private class ChangeVisibilityClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            popup.hide();
            changeVisibilityOfColumns(false);
            storeUserSettings();
        }
    }

    private void updateWidthSettings() {
        final CollectionColumnWidthActionContext context = new CollectionColumnWidthActionContext();
        context.setActionConfig(UserSettingsUtil.createActionConfig());
        context.setLink(Application.getInstance().getHistoryManager().getLink());
        context.setCollectionViewName(collectionViewName);
        Map<String, String> fieldWidthMap = createFieldWidthMap();
        context.setFieldWidthMap(fieldWidthMap);
        CollectionColumnWidthAction action =
                ComponentRegistry.instance.get(CollectionColumnWidthActionContext.COMPONENT_NAME);
        action.setInitialContext(context);
        action.perform();
    }

    private void updateOrderSettings() {
        final CollectionColumnOrderActionContext context = new CollectionColumnOrderActionContext();
        context.setLink(Application.getInstance().getHistoryManager().getLink());
        context.setCollectionViewName(collectionViewName);
        context.setActionConfig(UserSettingsUtil.createActionConfig());
        for (ColumnHeaderBlock columnHeaderBlock : columnHeaderBlocks) {
            if (columnHeaderBlock.getColumn().getFieldName() != null) {
                context.addOrder(columnHeaderBlock.getColumn().getFieldName());
            }
        }
        final Action action = ComponentRegistry.instance.get(CollectionColumnOrderActionContext.COMPONENT_NAME);
        action.setInitialContext(context);
        action.perform();

    }
    private int getOtherWidgetsDelta(){
        CompactModeState compactModeState = Application.getInstance().getCompactModeState();
        return compactModeState.isNavigationTreePanelExpanded() ? compactModeState.getSecondLevelNavigationPanelWidth() : 0;
    }
}

