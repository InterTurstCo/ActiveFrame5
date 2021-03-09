package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.SortCollectionState;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
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
    public static final int CALCULATION_INACCURACY = 1;// one pixel inaccuracy causes drawing fake scroll, removing 1px prevents scroll issue
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
    @Deprecated //plugin data contains boolean if plugin is embedded inside the widget
    public static boolean shouldSetSelection(CollectionPluginData collectionPluginData) {
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        return tableBrowserParams == null;
    }

    public static void adjustWidthWithoutUserSettingsKeeping(int tableWidth, CollectionDataGrid dataGrid){
        calculateColumnsWidth(tableWidth, dataGrid, false);
    }

    public static boolean shouldChangeScrollPosition(SortCollectionState sortState){
        return  sortState != null //sorting was executed
                && sortState.getOffset() == 0; // not scrolling already sorted rows

    }

    private static void calculateColumnsWidth(int realTableWidth, CollectionDataGrid tableBody, boolean keepUserWidth){
        int tableWidth = realTableWidth - CALCULATION_INACCURACY;
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
    @Deprecated /*use List<InitialFilterConfig> mergeInitialFiltersConfigs(List<InitialFilterConfig> current,
    List<InitialFilterConfig> previous,Map<String, CollectionColumnProperties> columnPropertiesMap) instead */
    public static List<InitialFilterConfig> mergeInitialFiltersConfigs(List<InitialFilterConfig> current,
                                                                       List<InitialFilterConfig> previous){
        if(WidgetUtil.isEmpty(previous)){
            return current;
        }
        List<InitialFilterConfig> result = new ArrayList<InitialFilterConfig>();
        for (final InitialFilterConfig currentConfig: current) {
            InitialFilterConfig previousConfig = GuiUtil.find(previous, new Predicate<InitialFilterConfig>() {
                @Override
                public boolean evaluate(InitialFilterConfig input) {
                    return input.getName().equalsIgnoreCase(currentConfig.getName());
                }
            });
            if(previousConfig == null){
                result.add(currentConfig);
            } else {
                previousConfig.setParamConfigs(currentConfig.getParamConfigs());
                result.add(previousConfig);
            }

        }
        return result;

    }
    public static List<InitialFilterConfig> mergeInitialFiltersConfigs(List<InitialFilterConfig> current,
                                                                       List<InitialFilterConfig> previous,
                                                                       Map<String, CollectionColumnProperties> columnPropertiesMap){
        if(WidgetUtil.isEmpty(previous)){
            return current;
        }
        Set<String> visibleUiFiltersNames = new HashSet<String>();
        for (CollectionColumnProperties collectionColumnProperties : columnPropertiesMap.values()) {
            Boolean hidden = (Boolean)collectionColumnProperties.getProperty(CollectionColumnProperties.HIDDEN);
            if(!hidden){
                String filterName = (String)collectionColumnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
                visibleUiFiltersNames.add(filterName);
            }
        }

        return mergeInitialFiltersConfigs(current, previous, visibleUiFiltersNames);

    }

    private static List<InitialFilterConfig> mergeInitialFiltersConfigs(List<InitialFilterConfig> current,
                                                                       List<InitialFilterConfig> previous,
                                                                       Set<String> visibleUiFiltersNames){
        List<String> currentNames = new ArrayList<>();
        for (InitialFilterConfig curr : current) {
            currentNames.add(curr.getName());
        }
        List<InitialFilterConfig> merged = new ArrayList<>(previous);
        Iterator<InitialFilterConfig> iterator = merged.iterator();
        while (iterator.hasNext()){
            InitialFilterConfig config = iterator.next();
            if( visibleUiFiltersNames.contains(config.getName()) || currentNames.contains(config.getName())){
                iterator.remove();
            }
        }
        merged.addAll(current);
        return merged;

    }

}
