package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.dom.client.Style;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class DisplayStyleBuilder {
    public static Style.Display getDisplayStyle(SelectionStyleConfig selectionStyleConfig) {
        Style.Display displayStyle;
        String howToDisplay = selectionStyleConfig == null ? null : selectionStyleConfig.getName();
        if (BusinessUniverseConstants.DISPLAY_STYLE_BlOCK.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.BLOCK;
        } else {
            displayStyle = Style.Display.INLINE_BLOCK;
        }
        return displayStyle;
    }
}
