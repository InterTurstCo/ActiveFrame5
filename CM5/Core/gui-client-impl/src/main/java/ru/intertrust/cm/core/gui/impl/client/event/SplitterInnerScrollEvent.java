package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 30.10.13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class SplitterInnerScrollEvent extends GwtEvent<SplitterInnerScrollEventHandler> {
    public static final Type<SplitterInnerScrollEventHandler> TYPE = new Type<SplitterInnerScrollEventHandler>();

    private int upperPanelHeight;
    private int upperPanelWidth;
    private int downPanelHeight;
    private int downPanelWidth;
    private boolean scrollState;


    public SplitterInnerScrollEvent(int upperPanelHeight, int upperPanelWidth, int downPanelHeight, int downPanelWidth/*, boolean scrollState*/) {
        this.upperPanelHeight = upperPanelHeight;
        this.upperPanelWidth = upperPanelWidth;
        this.downPanelHeight = downPanelHeight;
        this.downPanelWidth = downPanelWidth;
//        this.scrollState = scrollState;
    }

    public SplitterInnerScrollEvent(int upperPanelHeight, int downPanelHeight) {
        this.upperPanelHeight = upperPanelHeight;
        this.downPanelHeight = downPanelHeight;

    }

    public int getUpperPanelHeight() {
        return upperPanelHeight;
    }

    public int getDownPanelHeight() {
        return downPanelHeight;
    }

    public int getUpperPanelWidth() {
        return upperPanelWidth;
    }

    public int getDownPanelWidth() {
        return downPanelWidth;
    }

    public boolean isScrollState() {
        return scrollState;
    }

    @Override
    public Type<SplitterInnerScrollEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SplitterInnerScrollEventHandler handler) {
        handler.setScrollPanelHeight(this);
    }
}
