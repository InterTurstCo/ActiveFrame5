package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 09.12.13
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public interface SideBarResizeEventHandler extends EventHandler {

    public void sideBarFixPositionEvent(SideBarResizeEvent event);
}
