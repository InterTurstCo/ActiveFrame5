package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 07.10.14
 *         Time: 12:20
 */
public interface EnumerationMapProvider {
    Map<String, Value> getMap(WidgetContext widgetContext);
}
