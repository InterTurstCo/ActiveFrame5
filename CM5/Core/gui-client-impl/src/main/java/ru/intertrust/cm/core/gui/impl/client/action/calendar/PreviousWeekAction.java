package ru.intertrust.cm.core.gui.impl.client.action.calendar;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.calendar.CalendarActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 18:11.
 */
@ComponentName(CalendarActionContext.PREVIOUS_ACTION_NAME)
public class PreviousWeekAction extends Action {
    @Override
    protected void execute() {

    }

    @Override
    public Component createNew() {
        return new PreviousWeekAction();
    }
}
