package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.08.2016
 * Time: 15:12
 * To change this template use File | Settings | File and Code Templates.
 */
public interface NodeStateEventHandler extends EventHandler {
    void onNodeStateEvent(NodeStateEvent event);
}
