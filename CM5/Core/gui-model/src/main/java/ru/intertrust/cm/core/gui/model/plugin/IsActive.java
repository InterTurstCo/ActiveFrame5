package ru.intertrust.cm.core.gui.model.plugin;

/**
 * Интерфейс-маркер, определяющий факт того, что в плагине присутствует "Панель действий".
 *
 * @author Denis Mitavskiy
 *         Date: 27.08.13
 *         Time: 12:04
 */
public interface IsActive {

    <E extends PluginState> E getPluginState();

    void setPluginState(PluginState pluginState);
}
