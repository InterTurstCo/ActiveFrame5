package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Defines marker of state of plugin (full screen mode, editable etc).
 *
 * @author Sergey.Okolot
 */
public interface PluginState extends Dto {

    PluginState createClone();
}
