package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.gui.model.plugin.PluginState;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 10:14
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchySurferPluginState implements PluginState {
    @Override
    public PluginState createClone() {
        final HierarchySurferPluginState clone = new HierarchySurferPluginState();
        return clone;
    }
}
