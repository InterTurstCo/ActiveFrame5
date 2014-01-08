package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 26.12.13
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public class SideBarResizeEventStyle extends GwtEvent<SideBarResizeEventStyleHandler> {
    public static final Type<SideBarResizeEventStyleHandler> TYPE = new Type<SideBarResizeEventStyleHandler>();
    private boolean onMouseOver;
    private String styleBeforeMouseOver;
    private String styleMouseOver;
    private String styleAfterMouseOver;

    public SideBarResizeEventStyle(boolean onMouseOver, String styleBeforeMouseOver, String styleMouseOver, String styleAfterMouseOver){
        this.styleBeforeMouseOver = styleBeforeMouseOver;
        this.styleMouseOver = styleMouseOver;
        this.styleAfterMouseOver = styleMouseOver;
    }

    @Override
    public Type<SideBarResizeEventStyleHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SideBarResizeEventStyleHandler handler) {
        handler.sideBarSetStyleEvent(this);
    }

    public boolean isOnMouseOver() {
        return onMouseOver;
    }

    public void setOnMouseOver(boolean onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    public String getStyleBeforeMouseOver() {
        return styleBeforeMouseOver;
    }

    public void setStyleBeforeMouseOver(String styleBeforeMouseOver) {
        this.styleBeforeMouseOver = styleBeforeMouseOver;
    }

    public String getStyleMouseOver() {
        return styleMouseOver;
    }

    public void setStyleMouseOver(String styleMouseOver) {
        this.styleMouseOver = styleMouseOver;
    }

    public String getStyleAfterMouseOver() {
        return styleAfterMouseOver;
    }

    public void setStyleAfterMouseOver(String styleAfterMouseOver) {
        this.styleAfterMouseOver = styleAfterMouseOver;
    }
}
