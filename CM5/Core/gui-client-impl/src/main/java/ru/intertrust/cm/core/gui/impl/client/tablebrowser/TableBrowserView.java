package ru.intertrust.cm.core.gui.impl.client.tablebrowser;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserRowItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.11.13
 *         Time: 13:15
 */
public class TableBrowserView extends Composite {

    private VerticalPanel root;
    private HorizontalPanel horizontLine;
    private VerticalPanel selectedRowsContainer;
    private TextBox filterEditor;
    private Button openDialogButton;
    private Button okButton;
    private Button cancelButton;
    private DialogBox dialogBox;
    private CellTable<TableBrowserRowItem> table;
    private List<TableBrowserRowItem> selectedItems = new ArrayList<TableBrowserRowItem>();
    private List<TableBrowserRowItem> proposedItems = new ArrayList<TableBrowserRowItem>();
    private List<TableBrowserRowItem> temporaryItems = new ArrayList<TableBrowserRowItem>();
    private LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap = new LinkedHashMap<String, String>();
    private TableBrowserRowItem newSelectedItem; //the object selected by selectionModel
    private HorizontalPanel buttonsContainer;
    private FlowPanel dialogBoxContent;
    private boolean isSingleChoice;
    private int widgetWidth;

    public TableBrowserView() {
        init();
    }

    public TableBrowserView(LinkedHashMap<String, String> columnNamesAndDoFieldsMap) {
        this.domainObjectFieldsOnColumnNamesMap = columnNamesAndDoFieldsMap;
        init();

    }

    public void setWidgetWidth(int widgetWidth) {
        this.widgetWidth = widgetWidth;
    }

    public void setSingleChoice(boolean singleChoice) {
        isSingleChoice = singleChoice;
    }

    public List<TableBrowserRowItem> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<TableBrowserRowItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public void setProposedItems(List<TableBrowserRowItem> proposedItems) {
        this.proposedItems = proposedItems;
    }

    public DialogBox getDialogBox() {
        return dialogBox;
    }

    public Button getOpenDialogButton() {
        return openDialogButton;
    }

    public LinkedHashMap<String, String> getDomainObjectFieldsOnColumnNamesMap() {
        return domainObjectFieldsOnColumnNamesMap;
    }

    public void setDomainObjectFieldsOnColumnNamesMap(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap) {
        this.domainObjectFieldsOnColumnNamesMap = domainObjectFieldsOnColumnNamesMap;
    }

    public TextBox getFilterEditor() {
        return filterEditor;
    }

    public CellTable<TableBrowserRowItem> getTable() {
        return table;
    }

    public void init() {
        root = new VerticalPanel();
        horizontLine = new HorizontalPanel();
        selectedRowsContainer = new VerticalPanel();
        initDialogView();
        horizontLine.add(filterEditor);
        horizontLine.add(openDialogButton);
        root.add(horizontLine);
        root.add(selectedRowsContainer);
        initWidget(root);

    }

    public void buildTable() {
        if (isSingleChoice) {
            createTableWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap);
            addClickHandlersForSingleChoice();
        } else {
            createTableWithCheckBoxes(domainObjectFieldsOnColumnNamesMap);
            addClickHandlersForMultiplyChoice();
        }
        drawSelectedRows();
    }

    private void initDialogView() {
        filterEditor = new TextBox();
        openDialogButton = new Button("ADD");
        dialogBox = new DialogBox();
        table = new CellTable<TableBrowserRowItem>();
        okButton = new Button("OK");
        cancelButton = new Button("CANCEL");
        buttonsContainer = new HorizontalPanel();
        buttonsContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsContainer.add(okButton);
        buttonsContainer.add(cancelButton);
        dialogBoxContent = new FlowPanel();
        dialogBoxContent.add(table);
        dialogBoxContent.add(buttonsContainer);

        dialogBox.add(dialogBoxContent);

    }

    private void addCancelButtonClickHandler() {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
    }

    private void addClickHandlersForMultiplyChoice() {

        addCancelButtonClickHandler();
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (TableBrowserRowItem item : temporaryItems) {
                    selectedItems.add(item);
                }
                temporaryItems.clear();
                cleanUpSelectedRows();
                drawSelectedRows();
                dialogBox.hide();
            }
        });
    }

    private void addClickHandlersForSingleChoice() {
        addCancelButtonClickHandler();
        final NoSelectionModel<TableBrowserRowItem> selectionModel = new NoSelectionModel<TableBrowserRowItem>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                newSelectedItem = selectionModel.getLastSelectedObject();

            }
        });


        table.setSelectionModel(selectionModel);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                selectedItems.add(newSelectedItem);
                proposedItems.remove(newSelectedItem);
                selectionModel.setSelected(newSelectedItem, false);
                setTableData(proposedItems);
                table.redraw();
                cleanUpSelectedRows();
                drawSelectedRows();

            }
        });

    }

    public void drawSelectedRows() {
        for (TableBrowserRowItem row : selectedItems) {
            drawSelectedRow(row);
        }
    }

    private void drawSelectedRow(TableBrowserRowItem row) {

        Button moveUp = new Button("UP");
        Button moveDown = new Button("DOWN");
        Button delete = new Button("DELETE");
        HorizontalPanel rowPanel = new HorizontalPanel();

        rowPanel.add(new Label(row.getSelectedRowRepresentation()));
        rowPanel.getWidget(0).setWidth(widgetWidth * 0.9 + "px");
        rowPanel.add(moveUp);
        rowPanel.add(moveDown);
        rowPanel.add(delete);
        selectedRowsContainer.add(rowPanel);
        delete.addClickHandler(new DeleteSelectedModelHandler(row));
        moveUp.addClickHandler(new MoveUpHandler(rowPanel));
        moveDown.addClickHandler(new MoveDownHandler(rowPanel));

    }

    private TextColumn<TableBrowserRowItem> buildNameColumn(final String s) {

        return new TextColumn<TableBrowserRowItem>() {
            @Override
            public String getValue(TableBrowserRowItem object) {

                return object.getStringValue(s);
            }
        };
    }

    private void createTableWithCheckBoxes(LinkedHashMap<String, String> columnNamesAndDoFieldsMap) {

        Column<TableBrowserRowItem, Boolean> checkColumn = new Column<TableBrowserRowItem, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(TableBrowserRowItem object) {
                return false;
            }
        };

        checkColumn.setFieldUpdater(new FieldUpdater<TableBrowserRowItem, Boolean>() {
            @Override
            public void update(int index, TableBrowserRowItem object, Boolean value) {
                if (value) {
                    temporaryItems.add(object);
                } else {
                    if (temporaryItems.contains(object)) {
                        temporaryItems.remove(object);
                    }
                }
            }
        });
        table.addColumn(checkColumn, "");
        createTableWithoutCheckBoxes(columnNamesAndDoFieldsMap);

    }


    private void createTableWithoutCheckBoxes(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap) {

        int count = domainObjectFieldsOnColumnNamesMap.size() == 0 ? 1 : domainObjectFieldsOnColumnNamesMap.size();
        int columnSize = (widgetWidth / count);
        for (String field : domainObjectFieldsOnColumnNamesMap.keySet()) {
            Column<TableBrowserRowItem, String> column = buildNameColumn(field);
            String columnName = domainObjectFieldsOnColumnNamesMap.get(field);
            table.addColumn(column, columnName);
            table.setColumnWidth(column, columnSize + "px");
        }

    }

    public void setTableData(List<TableBrowserRowItem> newRows) {
        proposedItems = newRows;
        table.setRowData(newRows);
    }


    public void cleanUpSelectedRows() {
        selectedRowsContainer.clear();
    }

    private class MoveUpHandler implements ClickHandler {
        private HorizontalPanel rowPanel;

        public MoveUpHandler(HorizontalPanel rowPanel) {
            this.rowPanel = rowPanel;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (selectedRowsContainer.getWidgetIndex(rowPanel) > 0) {
                int index = selectedRowsContainer.getWidgetIndex(rowPanel);
                selectedRowsContainer.remove(index);
                selectedRowsContainer.insert(rowPanel, index - 1);
            }
        }
    }

    private class MoveDownHandler implements ClickHandler {
        private HorizontalPanel rowPanel;

        public MoveDownHandler(HorizontalPanel rowPanel) {
            this.rowPanel = rowPanel;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (selectedRowsContainer.getWidgetIndex(rowPanel) < selectedRowsContainer.getWidgetCount() - 1) {
                int index = selectedRowsContainer.getWidgetIndex(rowPanel);
                selectedRowsContainer.remove(index);
                selectedRowsContainer.insert(rowPanel, index + 1);
            }
        }
    }

    private class DeleteSelectedModelHandler implements ClickHandler {
        private TableBrowserRowItem model;

        public DeleteSelectedModelHandler(TableBrowserRowItem model) {
            this.model = model;
        }

        @Override
        public void onClick(ClickEvent event) {

            selectedItems.remove(model);
            Button deleteButton = (Button) event.getSource();
            deleteButton.getElement().getParentElement().getParentElement().removeFromParent();
        }
    }

}


