package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.util.List;

/**
 * Данные плагина, в котором существует панель действий.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:14
 */
public abstract class ActivePluginData extends PluginData {
    private List<ActionContext> actionContexts;
    private PluginState pluginState;

    /**
     * Возвращает список конфигураций действий, отображаемых в "Панели действий"
     * @return список конфигураций действий, отображаемых в "Панели действий"
     */
    public List<ActionContext> getActionContexts() {
        return actionContexts;
    }

    /**
     * Устанавливает список конфигураций действий, отображаемых в "Панели действий"
     * @param actionContexts список конфигураций действий, отображаемых в "Панели действий"
     */
    public void setActionContexts(List<ActionContext> actionContexts) {
        this.actionContexts = actionContexts;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    public void setPluginState(PluginState pluginState) {
        this.pluginState = pluginState;
    }
}
