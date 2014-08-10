package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Sergey.Okolot
 *         Created on 17.07.2014 17:15.
 */
public interface ActionManager {

    /**
     * Выыполняет действий по согласию пользователся, если имеются несохраненные данные.
     *
     */
    public void executeIfUserAgree(ConfirmCallback confirmCallback);

    boolean isEditorDirty();
}
