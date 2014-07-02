package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 15:54
 */
public class PluginData implements Dto {
    // @defaultUID
    private static final long serialVersionUID = 0L;

    private PluginState pluginState;

    public PluginState getPluginState() {
        return pluginState;
    }

    public void setPluginState(PluginState pluginState) {
        this.pluginState = pluginState;
    }
}
