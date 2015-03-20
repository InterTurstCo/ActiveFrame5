package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.01.2015
 *         Time: 18:43
 */
public class LeftSideDialogBoxResizablePanel extends ResizablePanel {
    private static final int DIALOG_BOX_MARGINS = 4;
    private DialogBox db;

    public LeftSideDialogBoxResizablePanel(DialogBox db, int minimalWidth, int minimalHeight, boolean keepScreenBoundaries) {
        super(minimalWidth, minimalHeight, keepScreenBoundaries);
        this.db = db;
    }

    protected boolean isCursorResize(Event event) {
        int cursorY = event.getClientY();
        int initialY = this.getAbsoluteTop();
        int height = this.getOffsetHeight();

        int cursorX = event.getClientX();
        int initialX = this.getAbsoluteLeft();

        int leftXCoordinate = initialX;
        int rightXCoordinate = initialX + RESIZE_AREA_LINEAR_SIZE;
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
            int centerResizeAreaXCoordinate = initialX + RESIZE_AREA_LINEAR_SIZE / 2;
            int centerResizeAreaYCoordinate = initialY + height - RESIZE_AREA_LINEAR_SIZE / 2;
            if(centerResizeAreaXCoordinate - OFFSET <= cursorX && centerResizeAreaYCoordinate  - OFFSET <= cursorY){
                return Style.Cursor.S_RESIZE;
            } else if(centerResizeAreaXCoordinate  + OFFSET <= cursorX && centerResizeAreaYCoordinate + OFFSET >= cursorY){
                return Style.Cursor.E_RESIZE;
            } else {
                return Style.Cursor.SW_RESIZE;}

        }
        else {
            return Style.Cursor.DEFAULT;
        }
    }
    protected void handleMouseMove(Event event) {

        //calculate and set the new size
        if (bDragDrop && doNotBreakBoundaries(event)) {
            Integer height = event.getClientY() - this.getAbsoluteTop();
            this.setHeight(height + "px");
            int width = this.getAbsoluteLeft() - event.getClientX() - DIALOG_BOX_MARGINS + this.getOffsetWidth();;
            db.setPopupPosition(event.getClientX(),db.getPopupTop());
            this.setWidth(width + "px");

            notifyPanelResizeListeners(this.getOffsetWidth(), height);
        }
    }

    protected boolean doNotBreakBoundaries(Event event){
        int absY = event.getClientY();
        int originalY = this.getAbsoluteTop();
        int screenMaxY = Window.getClientHeight();
        int width = this.getOffsetWidth();
        boolean minimalSizeCondition = absY > originalY + getMinimalHeight() && width >= getMinimalWidth();
        boolean screenCondition = !keepScreenBoundaries || absY <= screenMaxY;
        return minimalSizeCondition && screenCondition;
    }
    @Override
    protected String getResizeZoneStyleName() {
        return GlobalThemesManager.getCurrentTheme().commonCss().leftBottomResizeCursorArea();
    }
}
