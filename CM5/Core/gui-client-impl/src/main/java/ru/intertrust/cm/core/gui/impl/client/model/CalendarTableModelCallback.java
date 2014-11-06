package ru.intertrust.cm.core.gui.impl.client.model;

import java.util.List;

import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;

/**
 * @author Sergey.Okolot
 *         Created on 23.10.2014 15:10.
 */
public interface CalendarTableModelCallback {

    void fillValues(List<CalendarItemsData> values);
}
