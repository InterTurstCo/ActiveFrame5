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

    private int sideBarWidths;
    private String styleForLeftSector;
    private String styleForCenterSector;

    public SideBarResizeEvent(int sideBarWidths, String styleForLeftSector, String styleForCenterSector) {
        this.styleForLeftSector = styleForLeftSector;
        this.sideBarWidths = sideBarWidths;
        this.styleForCenterSector = styleForCenterSector;
    }

    @Override
    public Type<SideBarResizeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SideBarResizeEventHandler handler) {
        handler.sideBarFixPositionEvent(this);
    }

    public int getSideBarWidths() {
        return sideBarWidths;
    }

    public String getStyleForLeftSector() {
        return styleForLeftSector;
    }

    public String getStyleForCenterSector() {
        return styleForCenterSector;
    }
}
