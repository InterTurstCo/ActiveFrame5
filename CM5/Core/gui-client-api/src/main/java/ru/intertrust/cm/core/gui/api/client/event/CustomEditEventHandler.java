package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 06.07.2016
 * Time: 9:57
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CustomEditEventHandler extends EventHandler {
    void onEdit(CustomEditEvent event);
}
