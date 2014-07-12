package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.config.gui.form.widget.DisplayValuesAsLinksConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 12:18
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
