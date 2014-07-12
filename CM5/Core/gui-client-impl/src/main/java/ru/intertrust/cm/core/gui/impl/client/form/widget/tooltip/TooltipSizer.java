package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.PopupPanel;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.MAX_TOOLTIP_DEFAULT_HEIGHT;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.MAX_TOOLTIP_DEFAULT_WIDTH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 16:54
 */
public class TooltipSizer {
    private String maxTooltipWidth;
    private String maxTooltipHeight;

    public TooltipSizer(SelectionFiltersConfig selectionFiltersConfig) {
       initSizes(selectionFiltersConfig);
    }
    private void initSizes(SelectionFiltersConfig selectionFiltersConfig) {
        if (selectionFiltersConfig == null) {
            maxTooltipWidth = MAX_TOOLTIP_DEFAULT_WIDTH;
            maxTooltipHeight = MAX_TOOLTIP_DEFAULT_HEIGHT;
        }
        String maxWidthFromConfig = selectionFiltersConfig.getMaxTooltipWidth();
        maxTooltipWidth = maxWidthFromConfig == null ? MAX_TOOLTIP_DEFAULT_WIDTH : maxWidthFromConfig;

        String maxHeightFromConfig = selectionFiltersConfig.getMaxTooltipHeight();
        maxTooltipHeight = maxHeightFromConfig == null ? MAX_TOOLTIP_DEFAULT_HEIGHT : maxHeightFromConfig;
    }

    public void setWidgetBounds(PopupPanel popup){
        Style style = popup.getElement().getStyle();
        style.setProperty("maxWidth", maxTooltipWidth);
        style.setProperty("maxHeight", maxTooltipHeight);
    }

}
