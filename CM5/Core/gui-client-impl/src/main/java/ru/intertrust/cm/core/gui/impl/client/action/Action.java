package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * <p>
 * Действие, которое можно выполнить из плагина при помощи соответствующего элемент пользовательского интерфейса
 * (например, кнопкой или гиперссылкой).
 * </p>
 * <p>
 * Действие является компонентом GUI и должно быть именовано {@link ru.intertrust.cm.core.gui.model.ComponentName}.
 * </p>
 *
 * @author Denis Mitavskiy
 *         Date: 27.08.13
 *         Time: 16:13
 */
public abstract class Action extends BaseComponent {
    /**
     * Плагин, инициирующий данное действие
     */
    protected Plugin plugin;

    /**
     * Конфигурация данного действия
     */
    protected ActionContext initialContext;

    /**
     * Возвращает плагин, инициирующий данное действие
     * @return плагин, инициирующий данное действие
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Устанавливает плагин, инициирующий данное действие
     * @param plugin плагин, инициирующий данное действие
     */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Возвращает конфигурацию данного действия
     * @return конфигурация данного действия
     */
    public ActionContext getInitialContext() {
        return initialContext;
    }

    /**
     * Устанавливает конфигурацию данного действия
     * @param initialContext конфигурация данного действия
     */
    public void setInitialContext(ActionContext initialContext) {
        this.initialContext = initialContext;
    }

    public abstract void execute();

    public boolean shouldBeValidated() {
        return initialContext.getActionConfig().isValidate();
    }

    public boolean isValid() {
        return true;
    }
}
