package ru.intertrust.cm.core.gui.impl.client.event;

import ru.intertrust.cm.core.gui.impl.client.Plugin;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 18:18
 */
public class PluginViewCreatedEvent {
    private final Plugin plugin;

    public PluginViewCreatedEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
