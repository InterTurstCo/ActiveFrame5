package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 13:30
 * интерфейс обработчика удаления элемента коллекции (CollectionPluginView)
 */
public interface DeleteCollectionRowEventHandler extends EventHandler {
    void deleteCollectionRow(DeleteCollectionRowEvent event);
}
