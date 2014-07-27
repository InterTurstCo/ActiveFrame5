package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.02.14
 *         Time: 13:15
 */
public class CollectionDataGridUtils {

    private CollectionDataGridUtils() {}

    public static void adjustColumnsWidth(int tableWidth, CollectionDataGrid tableBody){
        final Map<CollectionColumn, Integer> widthMap = new HashMap<>();
        final List<CollectionColumn> unProcessingColumnList = new ArrayList<>();
        int processingColumnCount = 0;
        for (int index = 0; index < tableBody.getColumnCount(); index++) {
            final CollectionColumn column = (CollectionColumn) tableBody.getColumn(index);
            if (column.getWidth() > 0) {
                final int columnWidth = adjustWidth(column.getWidth(), column.getMinWidth(), column.getMaxWidth());
                column.setWidth(columnWidth);
                processingColumnCount++;
                tableWidth -= columnWidth;
                widthMap.put(column, columnWidth);
            } else {
                unProcessingColumnList.add(column);
            }
        }
        int columnWidthAverage = tableWidth / (tableBody.getColumnCount() - processingColumnCount);
        for (Iterator<CollectionColumn> it = unProcessingColumnList.iterator(); it.hasNext();) {
            final CollectionColumn column = it.next();
            if (column.getMinWidth() > columnWidthAverage) {
                widthMap.put(column, column.getMinWidth());
                tableWidth -= column.getMinWidth();
                processingColumnCount++;
                it.remove();
            } else if (column.getMaxWidth() < columnWidthAverage) {
                widthMap.put(column, column.getMaxWidth());
                tableWidth -= column.getMaxWidth();
                processingColumnCount++;
                it.remove();
            }
        }
        if (tableBody.getColumnCount() - processingColumnCount != 0) {
            columnWidthAverage = tableWidth / (tableBody.getColumnCount() - processingColumnCount);
        }

        if (columnWidthAverage < BusinessUniverseConstants.MIN_COLUMN_WIDTH) {
            columnWidthAverage = BusinessUniverseConstants.MIN_COLUMN_WIDTH;
        }
        for (Iterator<CollectionColumn> it = unProcessingColumnList.iterator(); it.hasNext();) {
            final CollectionColumn column = it.next();
            widthMap.put(column, columnWidthAverage);
        }
        for (Map.Entry<CollectionColumn, Integer> entry : widthMap.entrySet()) {
            tableBody.setColumnWidth(entry.getKey(), entry.getValue() + "px");
        }
    }

    private static int adjustWidth(int calculatedWidth, int minWidth, int maxWidth) {
        if (calculatedWidth < minWidth) {
            return minWidth;
        } else if (calculatedWidth > maxWidth) {
            return  maxWidth;
        }
        return calculatedWidth;
    }
}
