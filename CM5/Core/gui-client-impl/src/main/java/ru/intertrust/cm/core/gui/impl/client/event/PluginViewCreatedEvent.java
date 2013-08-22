package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 18:18
 */
public class PluginViewCreatedEvent extends GwtEvent<PluginViewCreatedEventHandler> {
    public static Type<PluginViewCreatedEventHandler> TYPE = new Type<PluginViewCreatedEventHandler>();
    private final Plugin plugin;

    public PluginViewCreatedEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Type<PluginViewCreatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PluginViewCreatedEventHandler handler) {
        handler.onPluginViewCreated(this);
    }
}
