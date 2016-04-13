package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 06.04.2016
 * Time: 16:42
 * To change this template use File | Settings | File and Code Templates.
 */
public interface BreadCrumbNavigationEventHandler extends EventHandler {
    public void onNavigation(BreadCrumbNavigationEvent event);
}
