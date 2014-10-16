package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName(CalendarPluginData.COMPONENT_NAME)
public class CalendarPlugin extends Plugin {

    @Override
    public PluginView createView() {
        return new CalendarPluginView(this);
    }

    @Override
    public Component createNew() {
        return new CalendarPlugin();
    }
}
