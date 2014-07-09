package ru.intertrust.cm.core.gui.impl.client.plugins;

/**
 *
 * @author Sergey.Okolot
 *         Created on 04.07.2014 16:13.
 */
public interface PluginHistorySupport {

    /**
     *
     * @return TRUE - Обработка события восстановления данных истории завершена и дальше по цепочке обработка не
     * требуется, FALSE - событие восстановления данных истории должно быть передано дальше по цепочкею
     */
    boolean restoreHistory();

//    void fillHistoryData();
}
