package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.01.2015
 *         Time: 10:27
 */
public class RightSideResizablePanel extends ResizablePanel {
    public RightSideResizablePanel(int minimalWidth, int minimalHeight, boolean keepScreenBoundaries, boolean notResizable) {
        super(minimalWidth, minimalHeight, keepScreenBoundaries, notResizable);
    }

    public RightSideResizablePanel(int minimalWidth, int minimalHeight, boolean keepScreenBoundaries) {
        super(minimalWidth, minimalHeight, keepScreenBoundaries);
    }

    protected boolean isCursorResize(Event event) {
        int cursorY = event.getClientY();
        int initialY = this.getAbsoluteTop();
        int height = this.getOffsetHeight();

        int cursorX = event.getClientX();
        int initialX = this.getAbsoluteLeft();
        int width = this.getOffsetWidth();

        int leftXCoordinate = initialX + width - RESIZE_AREA_LINEAR_SIZE;
        int rightXCoordinate = initialX + width;
        int topYCoordinate = initialY + height - RESIZE_AREA_LINEAR_SIZE;
        int bottomYCoordinate = initialY + height;
        if ((leftXCoordinate < cursorX && cursorX <= rightXCoordinate) &&
                (topYCoordinate < cursorY && cursorY <= bottomYCoordinate)){
            return true;
        }
        else {
            return false;
        }
    }

    protected Style.Cursor getCursor(Event event) {
        if(isCursorResize(event)){
            int cursorY = event.getClientY();
            int initialY = this.getAbsoluteTop();
            int height = this.getOffsetHeight();

            int cursorX = event.getClientX();
            int initialX = this.getAbsoluteLeft();
            int width = this.getOffsetWidth();
            int centerResizeAreaXCoordinate = initialX + width - RESIZE_AREA_LINEAR_SIZE / 2;
            int centerResizeAreaYCoordinate = initialY + height - RESIZE_AREA_LINEAR_SIZE / 2;
            if(centerResizeAreaXCoordinate - OFFSET >= cursorX && centerResizeAreaYCoordinate  - OFFSET <= cursorY){
                return Style.Cursor.S_RESIZE;
            } else if(centerResizeAreaXCoordinate  + OFFSET <= cursorX && centerResizeAreaYCoordinate + OFFSET >= cursorY){
                return Style.Cursor.E_RESIZE;
            } else {
                return Style.Cursor.SE_RESIZE;}

        }
        else {
            return Style.Cursor.DEFAULT;
        }
    }
    protected void handleMouseMove(Event event) {
            int height = event.getClientY() - this.getAbsoluteTop();
            this.setHeight(height + "px");
            int width = event.getClientX() - this.getAbsoluteLeft();
            this.setWidth(width + "px");
            notifyPanelResizeListeners(width, height);
    }

    protected boolean doNotBreakBoundaries(Event event){
        int absX = event.getClientX();
        int absY = event.getClientY();
        int originalX = this.getAbsoluteLeft();
        int originalY = this.getAbsoluteTop();
        int screenMaxY = Window.getClientHeight();
        int screenMaxX = Window.getClientWidth();
        boolean minimalSizeCondition = absY > originalY + getMinimalHeight() && absX > originalX + getMinimalWidth();
        boolean screenCondition = !keepScreenBoundaries || (absX <= screenMaxX && absY <= screenMaxY);
        return minimalSizeCondition && screenCondition;
    }
    @Override
    protected String getResizeZoneStyleName() {
        return GlobalThemesManager.getCurrentTheme().commonCss().rightBottomResizeCursorArea();
    }
}
