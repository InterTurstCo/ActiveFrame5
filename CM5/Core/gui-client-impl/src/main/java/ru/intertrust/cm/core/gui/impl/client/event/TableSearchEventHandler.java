package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 23.12.13
 * Time: 13:45
 * To change this template use File | Settings | File Templates.
 */
public interface TableSearchEventHandler extends EventHandler {
    void searchByFields(TableSearchEvent event);

}
