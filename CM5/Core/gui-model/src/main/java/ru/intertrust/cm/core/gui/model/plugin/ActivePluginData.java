package ru.intertrust.cm.core.gui.model.plugin;

import java.util.List;

import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.PluginActionEntryContext;

/**
 * Данные плагина, в котором существует панель действий.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:14
 */
public abstract class ActivePluginData extends PluginData {
    @Deprecated
    private List<ActionContext> actionContexts;
    private PluginState pluginState;
    private PluginActionEntryContext pluginActionContext;

    /**
     * @deprecated
     * Возвращает список конфигураций действий, отображаемых в "Панели действий"
     * @return список конфигураций действий, отображаемых в "Панели действий"
     */
    public List<ActionContext> getActionContexts() {
        return actionContexts;
    }

    /**
     * @deprecated
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

    public PluginActionEntryContext getPluginActionContext() {
        return pluginActionContext;
    }

    public void setPluginActionContext(final PluginActionEntryContext pluginActionContext) {
        this.pluginActionContext = pluginActionContext;
    }
}
