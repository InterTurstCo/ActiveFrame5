package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.09.2015
 */
public interface CollectionAddElementEventHandler  extends EventHandler {
    void onCollectionAddElement(CollectionAddElementEvent event);
}
