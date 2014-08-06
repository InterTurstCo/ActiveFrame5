package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import java.util.List;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnHiddenActionContext;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 21/03/14
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderController {
    private final String collectionName;
    private final String collectionViewName;
    private List<ColumnHeaderBlock> columnHeaderBlocks;
    private CollectionDataGrid dataGrid;
    private ColumnSelectorPopup popup;

    public CollectionColumnHeaderController(final String collectionName, final String collectionViewName,
                                            final CollectionDataGrid dataGrid) {
        this.collectionName = collectionName;
        this.collectionViewName = collectionViewName;
        this.dataGrid = dataGrid;

    }

    public List<ColumnHeaderBlock> getColumnHeaderBlocks() {
        return columnHeaderBlocks;
    }

    public void setColumnHeaderBlocks(List<ColumnHeaderBlock> columnHeaderBlocks) {
        this.columnHeaderBlocks = columnHeaderBlocks;
        changeVisibilityOfColumns();
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

    private void changeVisibilityOfColumns() {
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

    private void storeUserSettings() {
        final ActionConfig actionConfig = new ActionConfig();
        actionConfig.setImmediate(true);
        actionConfig.setDirtySensitivity(false);
        final CollectionColumnHiddenActionContext actionContext = new CollectionColumnHiddenActionContext();
        actionContext.setCollectionName(collectionName);
        actionContext.setActionConfig(actionConfig);
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
            popup.hide();
            changeVisibilityOfColumns();
            storeUserSettings();
        }
    }
}
