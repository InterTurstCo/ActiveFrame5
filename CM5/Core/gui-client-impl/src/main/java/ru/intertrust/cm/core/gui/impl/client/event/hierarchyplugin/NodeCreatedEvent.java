package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 19.08.2016
 * Time: 11:30
 * To change this template use File | Settings | File and Code Templates.
 */
public class NodeCreatedEvent extends GwtEvent<NodeCreatedEventHandler> {
    private String viewId;
    public static final Type<NodeCreatedEventHandler> TYPE = new Type<>();

    public NodeCreatedEvent(String aViewId){
        viewId = aViewId;
    }

    @Override
    public Type<NodeCreatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NodeCreatedEventHandler handler) {
        handler.onNodeCreatedEvent(this);
    }

    public String getViewId() {
        return viewId;
    }
}
