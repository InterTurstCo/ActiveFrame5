package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Sergey.Okolot
 *         Created on 17.07.2014 17:15.
 */
public interface ActionManager {

    /**
     * Если изменения данных отсутствуют, выполняется {@link ConfirmCallback#onAffirmative()} метод.<br/>
     * При наличии изменных данных выводит предупреждающий диалог и вызывается метод выбранный пользователем.
     *
     * @param confirmCallback instance of {@link ru.intertrust.cm.core.gui.api.client.ConfirmCallback} class.
     */
    public void checkChangesBeforeExecution(ConfirmCallback confirmCallback);
}
