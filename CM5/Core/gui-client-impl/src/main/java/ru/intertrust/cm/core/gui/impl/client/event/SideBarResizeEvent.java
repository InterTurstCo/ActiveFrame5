package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 09.12.13
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
public class SideBarResizeEvent extends GwtEvent<SideBarResizeEventHandler> {
    public static final Type<SideBarResizeEventHandler> TYPE = new Type<SideBarResizeEventHandler>();
    private boolean mousePush;
    private int sideBarWidts;

    public SideBarResizeEvent(boolean mousePush, int sideBarWidts) {
        this.mousePush = mousePush;
        this.sideBarWidts = sideBarWidts;
    }


    @Override
    public Type<SideBarResizeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SideBarResizeEventHandler handler) {
        handler.sideBarFixPositionEvent(this);
    }

    public int getSideBarWidts() {
        return sideBarWidts;
    }

    public void setSideBarWidts(int sideBarWidts) {
        this.sideBarWidts = sideBarWidts;
    }

    public boolean isMousePush() {
        return mousePush;
    }

    public void setMousePush(boolean mousePush) {
        this.mousePush = mousePush;
    }
}
