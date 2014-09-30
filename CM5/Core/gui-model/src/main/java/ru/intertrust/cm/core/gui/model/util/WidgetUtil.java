package ru.intertrust.cm.core.gui.model.util;

import ru.intertrust.cm.core.config.gui.form.widget.DisplayValuesAsLinksConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.09.2014
 *         Time: 23:49
 */
public class WidgetUtil {
    public static int getLimit(SelectionFiltersConfig selectionFiltersConfig) {
        int limit = 0;
        if (selectionFiltersConfig != null) {
            limit = selectionFiltersConfig.getRowLimit();
        }
        return limit;
    }

    public static boolean isDisplayingAsHyperlinks(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        return displayValuesAsLinksConfig != null && displayValuesAsLinksConfig.isValue();
    }

}