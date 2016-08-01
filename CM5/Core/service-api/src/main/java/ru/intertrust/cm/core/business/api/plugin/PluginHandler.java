package ru.intertrust.cm.core.business.api.plugin;

/**
 * Интерфейс рлагина
 * @author larin
 *
 */
public interface PluginHandler {
    /**
     * Выполняет алгоритм плагина
     * @param param
     * @return
     */
    String execute(String param);
}
