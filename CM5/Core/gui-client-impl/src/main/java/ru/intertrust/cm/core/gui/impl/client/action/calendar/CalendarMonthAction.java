package ru.intertrust.cm.core.gui.impl.client.action.calendar;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.calendar.CalendarActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 18:09.
 */
@ComponentName(CalendarActionContext.MONTH_ACTION_NAME)
public class CalendarMonthAction extends Action {

    @Override
    protected void execute() {
        final CalendarConfig config = (CalendarConfig) getPlugin().getConfig();
        config.setStartMode(CalendarConfig.MONTH_MODE);
        final Plugin calendar = ComponentRegistry.instance.get(config.getComponentName());
        calendar.setConfig(config);
        calendar.setNavigationConfig(plugin.getNavigationConfig());
        calendar.setDisplayActionToolBar(true);
        plugin.getOwner().open(calendar);
    }

    @Override
    public Component createNew() {
        return new CalendarMonthAction();
    }
}
