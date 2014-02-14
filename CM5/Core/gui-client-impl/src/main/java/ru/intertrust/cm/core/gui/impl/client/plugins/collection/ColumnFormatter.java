package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverterFactory;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.LabelCell;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.SortedMarker;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * Created by User on 12.02.14.
 */
public class ColumnFormatter {
    private static final String CUT_STYLE = "cut";
    public static Column<CollectionRowItem, ?> createFormattedColumn(CellTable<CollectionRowItem> tableHeader, CollectionColumnProperties columnProperties) {
        String field = (String) columnProperties.getProperty(CollectionColumnProperties.FIELD_NAME);
        String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
        converter.init(columnProperties.getProperties());
        String textBreakStyle = (String) columnProperties.getProperty(CollectionColumnProperties.TEXT_BREAK_STYLE);
        Column<CollectionRowItem, Label> column = new CollectionColumn(field, converter, new LabelCell(getCssStyleForText(textBreakStyle)));
        SortedMarker sortedMarker = (SortedMarker) columnProperties.getProperty(CollectionColumnProperties.SORTED_MARKER);
        if (sortedMarker != null) {
            boolean ascending = sortedMarker.isAscending();
            column.setDefaultSortAscending(ascending);
            tableHeader.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(column, ascending) );
        }
        String columnName = (String) columnProperties.getProperty(CollectionColumnProperties.NAME_KEY);
        column.setDataStoreName(columnName);
        boolean sortable = (Boolean) columnProperties.getProperty(CollectionColumnProperties.SORTABLE);
        column.setSortable(sortable);
        return column;
    }
    private static String getCssStyleForText(String textBreakStyle)  {
        if (CUT_STYLE.equalsIgnoreCase(textBreakStyle)) {
            return " style = \"overflow: hidden; text-overflow: ellipsis; white-space: nowrap;\"";

        }
        return "";
    }
}
