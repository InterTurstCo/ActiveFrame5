package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.ActionConfig;

import java.util.List;

/**
 * Данные плагина, в котором существует панель действий.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:14
 */
public abstract class ActivePluginData extends PluginData {
    private List<ActionConfig> actionConfigs;

    public List<ActionConfig> getActionConfigs() {
        return actionConfigs;
    }

    public void setActionConfigs(List<ActionConfig> actionConfigs) {
        this.actionConfigs = actionConfigs;
    }
}
