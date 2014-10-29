package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName(CalendarConfig.COMPONENT_NAME)
public class CalendarPlugin extends Plugin implements IsActive {

    private EventBus localEventBus = new SimpleEventBus();

    @Override
    public PluginView createView() {
        return new CalendarPluginView(this, localEventBus);
    }

    @Override
    public Component createNew() {
        return new CalendarPlugin();
    }

    @Override
    public <E extends PluginState> E getPluginState() {
        return null;
    }

    @Override
    public void setPluginState(PluginState pluginState) {

    }

    public EventBus getLocalEventBus() {
        return localEventBus;
    }
}
