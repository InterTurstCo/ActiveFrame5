package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.06.2015
 *         Time: 9:19
 */
public class SelfRegisteredEventHandler implements EventHandler {
    private HandlerRegistration handlerRegistration;

    public void setHandlerRegistration(HandlerRegistration handlerRegistration) {
        this.handlerRegistration = handlerRegistration;
    }
    protected void removeItself(){
        if(handlerRegistration != null){
            handlerRegistration.removeHandler();
        }
    }
}
