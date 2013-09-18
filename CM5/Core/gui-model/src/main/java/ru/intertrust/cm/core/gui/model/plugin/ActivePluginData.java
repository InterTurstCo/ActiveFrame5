package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.model.gui.ActionConfig;

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

    /**
     * Возвращает список конфигураций действий, отображаемых в "Панели действий"
     * @return список конфигураций действий, отображаемых в "Панели действий"
     */
    public List<ActionConfig> getActionConfigs() {
        return actionConfigs;
    }

    /**
     * Устанавливает список конфигураций действий, отображаемых в "Панели действий"
     * @param actionConfigs список конфигураций действий, отображаемых в "Панели действий"
     */
    public void setActionConfigs(List<ActionConfig> actionConfigs) {
        this.actionConfigs = actionConfigs;
    }
}
