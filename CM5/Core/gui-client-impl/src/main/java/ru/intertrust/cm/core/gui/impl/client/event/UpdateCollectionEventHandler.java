package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: IPetrov
 * Date: 04.12.13
 * Time: 10:54
 * интерфейс для обработки события обновления коллекции (CollectionPluginView)
 */
public interface UpdateCollectionEventHandler extends EventHandler {
    void updateCollection(UpdateCollectionEvent event);
}


