package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CalendarPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName("calendar.plugin")
public class CalendarPluginHandler extends PluginHandler {

    @Override
    public PluginData initialize(Dto param) {
        CalendarPluginData calendarPluginData = new CalendarPluginData();

        return calendarPluginData;
    }
}
