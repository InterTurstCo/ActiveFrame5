package ru.intertrust.cm.core.gui.model.plugin;

import java.io.Serializable;

/**
 * Defines marker of state of plugin (full screen mode, editable etc).
 *
 * @author Sergey.Okolot
 */
public interface PluginState extends Serializable {

    PluginState createClone();
}
