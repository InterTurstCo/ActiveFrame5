package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.PopupPanel;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 16:54
 */
public class TooltipSizer {

    public static void setWidgetBounds(WidgetConfig config, PopupPanel popup) {
        Style style = popup.getElement().getStyle();
        style.setProperty("maxWidth", config.getMaxTooltipWidth());
        style.setProperty("maxHeight", config.getMaxTooltipHeight());
    }

}
