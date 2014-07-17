package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Sergey.Okolot
 *         Created on 17.07.2014 17:15.
 */
public interface ActionManager {

    /**
     * Определяет возможность выполнения действий, если имеются несохраненные данные.
     * @return TRUE - выполнить действие, FALSE - запретить выполнение действия.
     */
    boolean isExecuteIfWorkplaceDirty();
}
