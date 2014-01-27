package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: IPetrov
 * Date: 21.01.14
 * Time: 17:43
 * Обработчик события завершения расширенного поиска
 */
public interface ExtendedSearchCompleteEventHandler extends EventHandler {
    void onExtendedSearchComplete(ExtendedSearchCompleteEvent event);
}
