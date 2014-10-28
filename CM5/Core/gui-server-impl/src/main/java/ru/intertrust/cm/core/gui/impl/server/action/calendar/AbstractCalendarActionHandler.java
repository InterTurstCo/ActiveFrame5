package ru.intertrust.cm.core.gui.impl.server.action.calendar;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.action.calendar.CalendarActionContext;
import ru.intertrust.cm.core.gui.model.action.calendar.CalendarActionData;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 17:51.
 */
public class AbstractCalendarActionHandler extends ActionHandler<CalendarActionContext, CalendarActionData> {

    @Override
    public CalendarActionData executeAction(CalendarActionContext context) {
        return new CalendarActionData();
    }

    @Override
    public CalendarHandlerStatusData getCheckStatusData() {
        return new CalendarHandlerStatusData();
    }

    @Override
    public CalendarActionContext getActionContext(ActionConfig actionConfig) {
        return new CalendarActionContext(actionConfig);
    }
}
