package ru.intertrust.cm.core.gui.model.action.calendar;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 17:54.
 */
public class CalendarActionContext extends ActionContext {

    public static final String WEEK_ACTION_NAME = "calendar.week.mode";
    public static final String MONTH_ACTION_NAME = "calendar.month.mode";
    public static final String PREVIOUS_ACTION_NAME = "calendar.previous.week";
    public static final String NEXT_ACTION_NAME = "calendar.next.week";
    public static final String TODAY_ACTION_NAME = "calendar.select.today";

    public CalendarActionContext() {
    }

    public CalendarActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }
}
