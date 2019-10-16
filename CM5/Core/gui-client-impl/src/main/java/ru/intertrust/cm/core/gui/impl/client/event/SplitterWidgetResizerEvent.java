package ru.intertrust.cm.core.gui.impl.client.event;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.11.13
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class SplitterWidgetResizerEvent extends GwtEvent<SplitterWidgetResizerEventHandler>{
    public static final Type<SplitterWidgetResizerEventHandler> EVENT_TYPE = new Type<SplitterWidgetResizerEventHandler>();

    private int firstWidgetWidth;
    private int secondWidgetWidth;
    private int firstWidgetHeight;
    private int secondWidgetHeight;
    private boolean type;
    private boolean arrowsPress;


    public SplitterWidgetResizerEvent(int firstWidgetWidth, int secondWidgetWidth,
               int firstWidgetHeight, int secondWidgetHeight, boolean type, boolean arrowsPress) {
        this.firstWidgetWidth = firstWidgetWidth;
        this.secondWidgetWidth = secondWidgetWidth;
        this.firstWidgetHeight = firstWidgetHeight;
        this.secondWidgetHeight = secondWidgetHeight;
        this.type = type;
        this.arrowsPress = arrowsPress;
    }

    @Override
    public Type<SplitterWidgetResizerEventHandler> getAssociatedType() {
        return EVENT_TYPE;
    }

    @Override
    protected void dispatch(SplitterWidgetResizerEventHandler handler) {

        handler.setWidgetSize(this);
    }


    public int getFirstWidgetWidth() {
        return firstWidgetWidth;
    }

    public int getSecondWidgetWidth() {
        return secondWidgetWidth;
    }

    public int getFirstWidgetHeight() {
        return firstWidgetHeight;
    }

    public int getSecondWidgetHeight() {
        return secondWidgetHeight;
    }

    public boolean isType() {
        return type;
    }

    public boolean isArrowsPress() {
        return arrowsPress;
    }
}
