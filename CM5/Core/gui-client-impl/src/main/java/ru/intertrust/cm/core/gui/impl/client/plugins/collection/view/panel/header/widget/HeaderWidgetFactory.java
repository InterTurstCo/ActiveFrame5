package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 22:41
 */
public class HeaderWidgetFactory {
    public static HeaderWidget getInstance(CollectionColumn column, CollectionColumnProperties columnProperties,
                                           List<String> initialFilterValues){
        String searchFilterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
        if(searchFilterName == null) {
            return new NoFilterHeaderWidget(column.getDataStoreName());
        }
        String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        if(TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType) || DATE_TIME_WITH_TIME_ZONE_TYPE.equalsIgnoreCase(fieldType)
                || DATE_TIME_TYPE.equalsIgnoreCase(fieldType)) {
            return  getInstanceDateHeaderWidget(column, columnProperties, initialFilterValues);
        }
        return new LiteralFilterHeaderWidget(column, columnProperties, initialFilterValues);
    }
    private static HeaderWidget getInstanceDateHeaderWidget(CollectionColumn column, CollectionColumnProperties
            columnProperties, List<String> initialFilterValues){
        boolean isDateRange = (Boolean) columnProperties.getProperty(CollectionColumnProperties.DATE_RANGE);
        if(isDateRange) {
            return new RangeDateHeaderWidget(column, columnProperties, initialFilterValues);
        }
        return new OneDateFilterHeaderWidget(column, columnProperties, initialFilterValues);
    }
}
