package ru.intertrust.cm.core.util;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:15
 */
public class ConfigurationUtil {
    public static String getWidgetTag(WidgetConfig widgetConfig) {
        return widgetConfig.getClass().getAnnotation(Root.class).name();
    }
}
