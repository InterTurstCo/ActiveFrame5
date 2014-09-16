package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2014
 *         Time: 0:21
 */
public class LinkedTableUtil {
    public static void configureEditableTable(LinkedDomainObjectsTableState currentState, CellTable<RowItem> table,
                                              ListDataProvider<RowItem> model, LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater) {

        configureNoneEditableTable(currentState, table);
        table.addColumn(buildEditButtonColumn(fieldUpdater), "");
        table.addColumn(buildDeleteButtonColumn(currentState, model), "");

    }

    public static void configureNoneEditableTable(LinkedDomainObjectsTableState currentState, CellTable<RowItem> table) {

        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        for (final SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            TextColumn<RowItem> column = new TextColumn<RowItem>() {
                @Override
                public String getValue(RowItem object) {
                    return object.getValueByKey(summaryTableColumnConfig.getWidgetId());
                }
            };
            table.addColumn(column, summaryTableColumnConfig.getHeader());
        }
    }

    private static Column<RowItem, String> buildEditButtonColumn(LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater) {
        ButtonCell editButton = new StyledButtonCell(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        Column<RowItem, String> editButtonColumn = new Column<RowItem, String>(editButton) {
            @Override
            public String getValue(RowItem object) {
                return "";
            }
        };
        editButtonColumn.setCellStyleNames(GlobalThemesManager.getCurrentTheme().commonCss().editColumn());
        editButtonColumn.setFieldUpdater(fieldUpdater);
        return editButtonColumn;
    }

    private static Column<RowItem, String> buildDeleteButtonColumn(final LinkedDomainObjectsTableState currentState, final ListDataProvider<RowItem> model) {
        ButtonCell deleteButton = new StyledButtonCell(GlobalThemesManager.getCurrentTheme().commonCss().deleteButton());
        Column<RowItem, String> deleteButtonColumn = new Column<RowItem, String>(deleteButton) {
            @Override
            public String getValue(RowItem object) {
                return "";
            }
        };
        deleteButtonColumn.setFieldUpdater(new FieldUpdater<RowItem, String>() {
            @Override
            public void update(int index, RowItem object, String value) {
                String stateKey = object.getParameter(BusinessUniverseConstants.STATE_KEY);
                if (stateKey != null) {
                    currentState.removeNewObjectState(stateKey);
                    currentState.removeEditedObjectState(stateKey);
                    model.getList().remove(object);
                } else {
                    // объекта нет в пуле, значит помечаем его для физического удаления
                    if (object.getObjectId() != null) {
                        currentState.getIds().remove(object.getObjectId());
                        currentState.getRowItems().remove(object);
                        model.getList().remove(object);
                    }
                }
            }
        });
        deleteButtonColumn.setCellStyleNames(GlobalThemesManager.getCurrentTheme().commonCss().deleteColumn());

        return deleteButtonColumn;
    }
}
