package ru.intertrust.cm.core.gui.model.util;

import ru.intertrust.cm.core.config.gui.form.widget.DisplayValuesAsLinksConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkEditingWidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ComplicatedParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TooltipWidgetState;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.09.2014
 *         Time: 23:49
 */
public class WidgetUtil {
    public static Integer getLimit(SelectionFiltersConfig selectionFiltersConfig) {
        int limit = -1;
        if (selectionFiltersConfig != null) {
            limit = selectionFiltersConfig.getRowLimit();
        }
        return limit;
    }

    public static boolean isDisplayingAsHyperlinks(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        return displayValuesAsLinksConfig != null && displayValuesAsLinksConfig.isValue();
    }

    public static Boolean isDisplayingHyperlinks(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        return displayValuesAsLinksConfig == null ? null : displayValuesAsLinksConfig.isValue();
    }

    public static boolean isNotEmpty(Collection coll) {
        return coll != null && !coll.isEmpty();
    }

    public static boolean isEmpty(Collection coll) {
        return !isNotEmpty(coll);
    }

    public static boolean isNotEmpty(Map map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return !isNotEmpty(map);
    }

    public static boolean containsOneElement(Collection collection){
        return collection != null && collection.size() == 1;
    }
    public static boolean isStringEmpty(String s){
        return s == null || s.isEmpty();
    }

    private static List<String> getWidgetIdsFromFilters(AbstractFiltersConfig<? extends AbstractFilterConfig> filtersConfig) {
        if(filtersConfig != null && isNotEmpty(filtersConfig.getFilterConfigs())){
        List<? extends AbstractFilterConfig> filtersConfigs = filtersConfig.getFilterConfigs();
            List<String> result = new ArrayList<String>();
            for (AbstractFilterConfig<? extends ComplicatedParamConfig> config : filtersConfigs) {
                fillWidgetsIds(config.getParamConfigs(), result);
            }

            return result;
        }
        return Collections.emptyList();
    }
    public static Collection<WidgetIdComponentName> getWidgetIdsComponentsNamesForFilters(AbstractFiltersConfig<? extends AbstractFilterConfig> filtersConfig,
                                                                                    Map<String, WidgetConfig> widgetConfigsById) {
        List<WidgetIdComponentName> result = new ArrayList<>();
        List<String> widgetIdsForFilters = getWidgetIdsFromFilters(filtersConfig);
        for (String widgetIdForFilter : widgetIdsForFilters) {
            WidgetConfig widgetConfig = widgetConfigsById.get(widgetIdForFilter);
            if(widgetConfig != null) {
                WidgetIdComponentName widgetIdComponentName = new WidgetIdComponentName(widgetIdForFilter, widgetConfig.getComponentName());
                result.add(widgetIdComponentName);
            }
        }
        return result;
    }

    private static void fillWidgetsIds(List<? extends ComplicatedParamConfig> paramConfigs, List<String> widgetsIds) {
        if(isNotEmpty(paramConfigs)){
            for (ComplicatedParamConfig paramConfig : paramConfigs) {
                String widgetId = paramConfig.getWidgetId();
                if(widgetId != null){
                    widgetsIds.add(widgetId);
                }
            }
        }

    }
    public static boolean shouldDrawTooltipButton(TooltipWidgetState state) {
        return shouldDrawTooltipButton(state,0);
    }

    public static boolean shouldDrawTooltipButton(TooltipWidgetState state, int delta) {
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        return config.getSelectionFiltersConfig() != null
                && config.getSelectionFiltersConfig().getRowLimit() != -1
                && state.getFilteredItemsNumber() + delta > config.getSelectionFiltersConfig().getRowLimit();
    }

    public static boolean drawAsTable(SelectionStyleConfig selectionStyleConfig) {
        return selectionStyleConfig != null
                && SelectionStyleConfig.Type.TABLE.equals(SelectionStyleConfig.Type.forName(selectionStyleConfig.getName()));
    }

}
