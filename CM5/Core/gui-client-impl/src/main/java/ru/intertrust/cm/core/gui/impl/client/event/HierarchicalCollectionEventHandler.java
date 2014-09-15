package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Lesia Puhova
 *         Date: 12.09.14
 *         Time: 18:48
 */
public interface HierarchicalCollectionEventHandler extends EventHandler {

    void onExpandHierarchyEvent(HierarchicalCollectionEvent event);

}
