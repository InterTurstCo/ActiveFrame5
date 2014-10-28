package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarViewConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;

/**
 * @author Sergey.Okolot
 *         Created on 28.10.2014 15:13.
 */
public interface CalendarItemRenderer extends ComponentHandler {

    CalendarItemData renderItem(IdentifiableObject identifiableObject, CalendarViewConfig config);
}
