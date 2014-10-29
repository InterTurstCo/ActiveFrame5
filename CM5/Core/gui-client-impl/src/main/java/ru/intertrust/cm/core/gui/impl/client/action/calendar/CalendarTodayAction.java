package ru.intertrust.cm.core.gui.impl.client.action.calendar;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.calendar.CalendarActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 18:08.
 */
@ComponentName(CalendarActionContext.TODAY_ACTION_NAME)
public class CalendarTodayAction extends Action {

    @Override
    protected void execute() {
        plugin.getLocalEventBus().fireEvent(new CalendarTodayEvent());
    }

    @Override
    public Component createNew() {
        return new CalendarTodayAction();
    }
}
