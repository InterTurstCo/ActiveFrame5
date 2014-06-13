package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.action.ToolbarContext;

/**
 * Данные плагина, в котором существует панель действий.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:14
 */
public abstract class ActivePluginData extends PluginData {
    private ToolbarContext toolbarContext;

    /**
     * Возвращает список конфигураций действий, отображаемых в "Панели действий"
     * @return список конфигураций действий, отображаемых в "Панели действий"
     * @return
     */
    public ToolbarContext getToolbarContext() {
        return toolbarContext;
    }

    /**
     * Устанавливает список конфигураций действий, отображаемых в "Панели действий"
     * @param toolbarContext
     */
    public void setToolbarContext(final ToolbarContext toolbarContext) {
        this.toolbarContext = toolbarContext;
    }
}
