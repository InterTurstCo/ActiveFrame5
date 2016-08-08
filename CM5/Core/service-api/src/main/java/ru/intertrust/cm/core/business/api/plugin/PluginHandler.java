package ru.intertrust.cm.core.business.api.plugin;

import javax.ejb.EJBContext;

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
    String execute(EJBContext context, String param);
}
