package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.08.2016
 * Time: 15:07
 * To change this template use File | Settings | File and Code Templates.
 */
public class AutoOpenedEvent extends GwtEvent<AutoOpenedEventHandler> {

    private String viewId;
    public static final Type<AutoOpenedEventHandler> TYPE = new Type<>();


    public AutoOpenedEvent(String aViewId){
            viewId = aViewId;
    }

    @Override
    public Type<AutoOpenedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AutoOpenedEventHandler handler) {
        handler.onAutoOpenedEvent(this);
    }

    public String getViewId() {
        return viewId;
    }
}
