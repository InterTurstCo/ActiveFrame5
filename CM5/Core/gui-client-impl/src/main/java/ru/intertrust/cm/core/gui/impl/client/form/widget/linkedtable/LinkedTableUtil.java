package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.impl.client.event.linkedtable.LinkedTableRowDeletedEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2014
 *         Time: 0:21
 */
public class LinkedTableUtil {
    public static void configureEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table,
                                              LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater,
                                              EventBus localEventBus) {
        configureNoneEditableTable(summaryTableConfig, table);
        table.addColumn(buildEditButtonColumn(fieldUpdater), "");
        table.addColumn(buildDeleteButtonColumn(localEventBus, fieldUpdater.isTooltipContent()), "");

    }

    public static void configureNoneEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table) {
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

    private static Column<RowItem, String> buildDeleteButtonColumn(final EventBus localEventBus, final boolean tooltipContent) {
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
                localEventBus.fireEvent(new LinkedTableRowDeletedEvent(object, tooltipContent));

            }
        });
        deleteButtonColumn.setCellStyleNames(GlobalThemesManager.getCurrentTheme().commonCss().deleteColumn());

        return deleteButtonColumn;
    }
}
