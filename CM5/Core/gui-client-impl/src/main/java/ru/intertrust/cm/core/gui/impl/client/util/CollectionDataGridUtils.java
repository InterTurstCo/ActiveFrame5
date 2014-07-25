package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.02.14
 *         Time: 13:15
 */
public class CollectionDataGridUtils {

    private CollectionDataGridUtils() {}

    public static void adjustColumnsWidth(int tableWidth, CollectionDataGrid tableBody){
        int numberOfColumns = tableBody.getColumnCount();
        int numberOfColumnsForIteration = numberOfColumns;
        int tableWidthAvailable = tableWidth;
        int tableWidthAvailableForRecalculating = tableWidth;
        Map<CollectionColumn, Integer> columnWithMinWidth = new HashMap<CollectionColumn, Integer>();
        Map<CollectionColumn, Integer> columnFirstCalculationMap = new HashMap<CollectionColumn, Integer>();
        for (int i = 0; i < numberOfColumnsForIteration; i++ ) {
            CollectionColumn column = (CollectionColumn) tableBody.getColumn(i);
            int columnWidthAverage = (tableWidthAvailable / numberOfColumns);
            int columnWidth = CollectionDataGridUtils.adjustWidth(columnWidthAverage, column.getMinWidth(), column.getMaxWidth());
            tableWidthAvailable -= columnWidth;
            numberOfColumns -= 1;
            if (columnWidth == column.getMinWidth()) {
                columnWithMinWidth.put(column, columnWidth);
                tableWidthAvailableForRecalculating -= columnWidth;
            } else {
                columnFirstCalculationMap.put(column, columnWidth) ;
            }

        }
        Map<CollectionColumn, Integer> columnCalculationFirstMapCorrected = recalculateColumnWidth(tableWidthAvailableForRecalculating, columnFirstCalculationMap.keySet());

        Set<CollectionColumn> columns = columnCalculationFirstMapCorrected.keySet();
        Map<CollectionColumn, Integer> columnSecondCalculationMap = new HashMap<CollectionColumn, Integer>();
        for(CollectionColumn column : columns){
            int columnWidth = columnCalculationFirstMapCorrected.get(column);

            if (columnWidth == column.getMinWidth()) {
                columnWithMinWidth.put(column, columnWidth);
                tableWidthAvailableForRecalculating -= columnWidth;
            } else {
                columnSecondCalculationMap.put(column, columnWidth) ;
            }

        }
        Map<CollectionColumn, Integer> columnCalculationSecondMapCorrected = recalculateColumnWidth(tableWidthAvailableForRecalculating, columnSecondCalculationMap.keySet());
        columnWithMinWidth.putAll(columnCalculationSecondMapCorrected);
        for (CollectionColumn column : columnWithMinWidth.keySet()) {
            int calculatedWidth = columnWithMinWidth.get(column);
            tableBody.setColumnWidth(column, calculatedWidth + "px");
        }
    }

    private static Map<CollectionColumn, Integer> recalculateColumnWidth(int width, Set<CollectionColumn> columns) {
        Map<CollectionColumn, Integer> columnCalculationMapCorrected = new HashMap<CollectionColumn, Integer>();
        int tableWidthAvailable = width;
        int numberOfColumns = columns.size();
        for(CollectionColumn column : columns){
            int columnWidthAverage = (tableWidthAvailable / numberOfColumns);
            int columnWidth = CollectionDataGridUtils.adjustWidth(columnWidthAverage, column.getMinWidth(), column.getMaxWidth());
            tableWidthAvailable -= columnWidth;
            numberOfColumns -= 1;
            columnCalculationMapCorrected.put(column, columnWidth);
        }
        return  columnCalculationMapCorrected;
    }

    private static int adjustWidth(int calculatedWidth, int minWidth, int maxWidth) {
        if (calculatedWidth < minWidth) {
            return minWidth;
        }
        if (calculatedWidth > maxWidth) {
            return  maxWidth;
        }
        return  calculatedWidth;
    }
}
