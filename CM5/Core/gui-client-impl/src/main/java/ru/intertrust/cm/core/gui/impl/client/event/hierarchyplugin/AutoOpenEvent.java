package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.08.2016
 * Time: 12:08
 * To change this template use File | Settings | File and Code Templates.
 */
public class AutoOpenEvent extends GwtEvent<AutoOpenEventHandler> {

    private String viewId;
    public static final Type<AutoOpenEventHandler> TYPE = new Type<>();

    public AutoOpenEvent(String aViewId){
        viewId = aViewId;
    }

    @Override
    public Type<AutoOpenEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AutoOpenEventHandler handler) {
        handler.onAutoOpenEvent(this);
    }

    public String getViewId() {
        return viewId;
    }
}
