package ru.intertrust.cm.core.gui.impl.client.panel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.12.2014
 *         Time: 9:19
 */

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;
import ru.intertrust.cm.core.gui.model.GuiException;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Cursor;


public abstract class ResizablePanel extends AbsolutePanel {
    private static final int DEFAULT_MINIMAL_WIDTH = 200;
    private static final int DEFAULT_MINIMAL_HEIGHT = 120;

    protected static final int RESIZE_AREA_LINEAR_SIZE = 20;
    protected static final int OFFSET = 2;

    private int minimalWidth;
    private int minimalHeight;

    protected boolean bDragDrop;
    protected boolean keepScreenBoundaries;
    protected boolean resizable;

    private List<PanelResizeListener> panelResizeListeners;

    public ResizablePanel(boolean resizable) {
        super();
        this.resizable = resizable;
        panelResizeListeners = new ArrayList<PanelResizeListener>();
        //listen to mouse-events
        DOM.sinkEvents(this.getElement(),
                Event.ONMOUSEDOWN |
                        Event.ONMOUSEMOVE |
                        Event.ONMOUSEUP |
                        Event.ONMOUSEOVER
        );
    }

    public ResizablePanel(int minimalWidth, int minimalHeight, boolean keepScreenBoundaries) {
        this(false);
        this.keepScreenBoundaries = keepScreenBoundaries;
        this.minimalWidth = minimalWidth;
        this.minimalHeight = minimalHeight;
    }

    public ResizablePanel(int minimalWidth, int minimalHeight, boolean keepScreenBoundaries, boolean resizable) {
        this(resizable);
        this.keepScreenBoundaries = keepScreenBoundaries;
        this.minimalWidth = minimalWidth;
        this.minimalHeight = minimalHeight;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * returns if mousepointer is in region to show cursor-resize
     *
     * @param event
     * @return true if in region
     */
    protected abstract boolean isCursorResize(Event event);

    protected abstract Cursor getCursor(Event event);

    protected abstract void handleMouseMove(Event event);

    protected abstract boolean doNotBreakBoundaries(Event event);

    protected abstract String getResizeZoneStyleName();

    /**
     * processes the mouse-events to show cursor or change states
     * - mouseover
     * - mousedown
     * - mouseup
     * - mousemove
     */
    @Override
    public void onBrowserEvent(Event event) {
        if (resizable) {
            final int eventType = DOM.eventGetType(event);

            switch (eventType) {
                case Event.ONMOUSEOVER:
                    handleMouseOver(event);
                    break;
                case Event.ONMOUSEDOWN:
                    handleMouseDown(event);
                    break;
                case Event.ONMOUSEMOVE:
                    //reset cursor-type
                    handleMouseMove(event);
                    break;
                case Event.ONMOUSEUP:
                    handleMouseUp();
                    break;
            }
            event.preventDefault();
            event.stopPropagation();
        } else {
            super.onBrowserEvent(event);
        }
    }

    private void handleMouseOver(Event event) {

        changeCursorStyle(getCursor(event));

    }

    private void handleMouseDown(Event event) {
        if (isCursorResize(event)) {
            //enable/disable resize
            if (bDragDrop == false) {
                bDragDrop = true;
                DOM.setCapture(this.getElement());
                RootPanel.getBodyElement().getStyle().setCursor(getCursor(event));
            }
        }
    }

    private void handleMouseUp() {
        bDragDrop = false;
        DOM.releaseCapture(this.getElement());
        RootPanel.getBodyElement().getStyle().setCursor(Cursor.DEFAULT);
    }


    private void changeCursorStyle(Cursor cursor) {
        this.getElement().getStyle().setCursor(cursor);
    }

    public void addResizeListener(PanelResizeListener panelResizeListener) {

        panelResizeListeners.add(panelResizeListener);
    }

    protected void notifyPanelResizeListeners(int width, int height) {
        for (PanelResizeListener panelResizeListener : panelResizeListeners) {
            panelResizeListener.onPanelResize(width, height);
        }
    }


    public void wrapWidget(Widget w) {
        if (getWidgetCount() > 0) {
            throw new GuiException("ResizablePanel should contain only one child");
        }
        super.add(w);
        if (resizable) {
            Panel resizeCursorPanel = new AbsolutePanel();
            resizeCursorPanel.addStyleName(getResizeZoneStyleName());
            super.add(resizeCursorPanel);
        }

    }

    protected int getMinimalWidth() {
        return minimalWidth == 0 ? DEFAULT_MINIMAL_WIDTH : minimalWidth;
    }

    protected int getMinimalHeight() {
        return minimalHeight == 0 ? DEFAULT_MINIMAL_HEIGHT : minimalHeight;
    }

}
