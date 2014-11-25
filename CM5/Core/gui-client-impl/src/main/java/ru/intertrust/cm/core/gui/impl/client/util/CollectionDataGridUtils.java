package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.MIN_COLUMN_WIDTH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.02.14
 *         Time: 13:15
 */
public class CollectionDataGridUtils {

    private CollectionDataGridUtils() {
    }

    public static void adjustWidthUserSettingsKeeping(int tableWidth, CollectionDataGrid dataGrid) {
        calculateColumnsWidth(tableWidth, dataGrid, true);
    }

    @Deprecated
    private static int adjustWidth(int calculatedWidth, int minWidth, int maxWidth) {
        if (calculatedWidth < minWidth) {
            return minWidth;
        } else if (calculatedWidth > maxWidth) {
            return maxWidth;
        }
        return calculatedWidth;
    }

    public static boolean isTableVerticalScrollNotVisible(CollectionDataGrid dataGrid) {
        int scrollMinVertical = dataGrid.getScrollPanel().getMinimumVerticalScrollPosition();
        int scrollMaxVertical = dataGrid.getScrollPanel().getMaximumVerticalScrollPosition();
        return scrollMinVertical == scrollMaxVertical;
    }

    public static boolean isTableHorizontalScrollNotVisible(CollectionDataGrid dataGrid) {
        int scrollMinHorizontal = dataGrid.getScrollPanel().getMinimumHorizontalScrollPosition();
        int scrollMaxHorizontal = dataGrid.getScrollPanel().getMaximumHorizontalScrollPosition();
        return scrollMinHorizontal == scrollMaxHorizontal;
    }

    public static boolean shouldSetSelection(CollectionPluginData collectionPluginData) {
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        return tableBrowserParams == null;
    }

    public static void adjustWidthWithoutUserSettingsKeeping(int tableWidth, CollectionDataGrid dataGrid){
        calculateColumnsWidth(tableWidth, dataGrid, false);
    }

    private static void calculateColumnsWidth(int tableWidth, CollectionDataGrid tableBody, boolean keepUserWidth){
        final Map<CollectionColumn, Integer> widthMap = new HashMap<>();
        final List<CollectionColumn> unProcessingColumnList = new ArrayList<>();
        int processedColumnCount = 0;
        for (int index = 0; index < tableBody.getColumnCount(); index++) {
            final CollectionColumn column = (CollectionColumn) tableBody.getColumn(index);
            if (!column.isVisible()) {
                //nothing anymore:)
            } else if (column.getUserWidth() > 0 && keepUserWidth) {
                int columnWidth = column.getUserWidth();
                column.setUserWidth(columnWidth);
                column.setDrawWidth(columnWidth);
                tableWidth -= columnWidth;
                processedColumnCount++;
                widthMap.put(column, columnWidth);
            } else {
                unProcessingColumnList.add(column);
            }
        }
        int unfinishedColumnCount = tableBody.getColumnCount() - processedColumnCount;
        int columnWidthAverage = unfinishedColumnCount < 1 ? 0 : tableWidth / unfinishedColumnCount;
        for (Iterator<CollectionColumn> it = unProcessingColumnList.iterator(); it.hasNext(); ) {
            final CollectionColumn column = it.next();
            if (column.getMinWidth() > columnWidthAverage) {
                widthMap.put(column, column.getMinWidth());
                tableWidth -= column.getMinWidth();
                processedColumnCount++;
                it.remove();
            } else if (column.getMaxWidth() < columnWidthAverage) {
                widthMap.put(column, column.getMaxWidth());

                tableWidth -= column.getMaxWidth();
                processedColumnCount++;
                it.remove();
            }
        }
        unfinishedColumnCount = tableBody.getColumnCount() - processedColumnCount;
        columnWidthAverage = unfinishedColumnCount < 1 ? 0 : tableWidth / unfinishedColumnCount;
        if (columnWidthAverage < MIN_COLUMN_WIDTH) {
            columnWidthAverage = MIN_COLUMN_WIDTH;
        }
        for (CollectionColumn column : unProcessingColumnList) {
            widthMap.put(column, columnWidthAverage);
        }
        for (Map.Entry<CollectionColumn, Integer> entry : widthMap.entrySet()) {
            CollectionColumn column = entry.getKey();
            int calculatedWidth = entry.getValue();
            tableBody.setColumnWidth(column, calculatedWidth + "px");
            column.setDrawWidth(calculatedWidth);
        }
    }

    public static void mergeInitialFiltersConfigs(List<InitialFilterConfig> current,
                                                                       List<InitialFilterConfig> previous){
        if(WidgetUtil.isEmpty(previous)){
            return;
        }
        Collection<String> names = getFilterNames(current);
        for (InitialFilterConfig config: previous) {
            if(!names.contains(config.getName())){
                current.add(config);
            }
        }

    }

    private static Collection<String> getFilterNames(List<InitialFilterConfig> configs){
        Set<String> result = new HashSet<>();
        if(WidgetUtil.isNotEmpty(configs)){
        for (InitialFilterConfig config : configs) {
            result.add(config.getName());
        }
        }
        return result;
    }

}
