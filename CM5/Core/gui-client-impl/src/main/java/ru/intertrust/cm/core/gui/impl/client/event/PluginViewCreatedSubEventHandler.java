package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 15.11.13
 * Time: 14:06
 * To change this template use File | Settings | File Templates.
 */
public interface PluginViewCreatedSubEventHandler extends EventHandler {
    public void setSizes(float widthRatio, float heightRatio);
}
