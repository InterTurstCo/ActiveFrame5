package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 19.08.2016
 * Time: 11:30
 * To change this template use File | Settings | File and Code Templates.
 */
public interface NodeCreatedEventHandler extends EventHandler {
    void onNodeCreatedEvent(NodeCreatedEvent event);
}
