package ru.intertrust.cm.core.gui.model.plugin.listplugin;

import ru.intertrust.cm.core.gui.model.plugin.PluginState;

/**
 * Created by Ravil on 11.04.2017.
 */
public class ListSurferPluginState implements PluginState {
    @Override
    public PluginState createClone() {
        final ListSurferPluginState clone = new ListSurferPluginState();
        return clone;
    }
}
