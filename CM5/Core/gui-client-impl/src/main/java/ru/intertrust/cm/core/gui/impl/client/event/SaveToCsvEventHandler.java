package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 18.01.14
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public interface SaveToCsvEventHandler  extends EventHandler{

    public void saveToCsv(SaveToCsvEvent saveToCsvEvent);
}
