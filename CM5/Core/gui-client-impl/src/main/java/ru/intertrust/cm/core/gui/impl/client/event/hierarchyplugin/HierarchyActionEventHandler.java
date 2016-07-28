package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 28.07.2016
 * Time: 11:18
 * To change this template use File | Settings | File and Code Templates.
 */
public interface HierarchyActionEventHandler extends EventHandler {
    void onHierarchyActionEvent(HierarchyActionEvent event);
}
