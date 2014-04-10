package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName("calendar.plugin")
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
