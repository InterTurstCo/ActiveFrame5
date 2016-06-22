package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 10:11
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CustomDeleteEventHandler extends EventHandler {
    enum DeleteStatus {OK, ERROR};
    void onDelete(CustomDeleteEvent event);
}
