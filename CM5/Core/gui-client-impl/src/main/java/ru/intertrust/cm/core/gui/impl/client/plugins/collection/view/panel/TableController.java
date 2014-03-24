package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.DOM;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseUtils;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

import java.util.HashMap;
import java.util.Map;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_COLUMN_NAME;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 30.09.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class TableController implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler {

    private int ownerWidth;
    private int resizeModifier;
    private EventBus eventBus;
    static final int COLUMN_MIN_WIDTH = 200;
    private Point startResizePoint;
    private Point startMovePoint;
    private int resizeStartPoint;
    private int resizeEndPoint;
    private boolean mouseDown = false;

    private int colIdx;
    private static int DELTA_X = 7;
    private static int DELTA_DRAG = 1;
    private static Style.Cursor MOVE_CURSOR = Style.Cursor.MOVE;
    private static Style.Cursor RESIZE_CURSOR = Style.Cursor.COL_RESIZE;
    private static Style.Cursor DEFAULT_CURSOR = Style.Cursor.DEFAULT;
    private static Style.Cursor POINTER_CURSOR = Style.Cursor.POINTER;
    private Column lastColumn;

    private Map<String, Boolean> sortMarkers = new HashMap<String, Boolean>();
    
    DataGrid<CollectionRowItem> body;

    public TableController( DataGrid<CollectionRowItem> body, EventBus eventBus) {

       
        this.body = body;
        this.eventBus = eventBus;

      /*  body.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOVER);
        body.addHandler(this, MouseDownEvent.getType());
        body.addHandler(this, MouseOverEvent.getType());
        body.addHandler(this, MouseMoveEvent.getType());
        body.addHandler(this, MouseUpEvent.getType());*/

    }

    // изменение ширины столбцов на части
    public void columnWindowResize(int width) {
        int count = body.getColumnCount();
        for (int i = 0; i < count; i++) {
            CollectionColumn headCol = (CollectionColumn) body.getColumn(i);
            CollectionColumn bodyCol = (CollectionColumn) body.getColumn(i);
            int adjustedWidth = BusinessUniverseUtils.adjustWidth(width, headCol.getMinWidth(), headCol.getMaxWidth());
            body.setColumnWidth(headCol, adjustedWidth + "px");
            body.setColumnWidth(bodyCol, adjustedWidth + "px");

        }

        searchPanelResize();

    }


    @Override
    public void onMouseDown(MouseDownEvent event) {

        onMouseDownEvent(event);
        event.preventDefault();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {

        onMouseUpEvent(event);
        event.preventDefault();
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {

        onMouseMoveEvent(event);
        event.preventDefault();
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        if (!isEventInHeaderArea(event)) {
            return;
        }
        onMouseOverEvent(event);
        event.preventDefault();
    }

    private void onMouseDownEvent(final MouseDownEvent e) {
        if (!isEventInHeaderArea(e)) {
            return;
        }
        Point p = getPoint(e);
        colIdx = getCellIdxByBorderPos(p);
        if (colIdx >= 0) {
            resizeStartPoint = (int) p.getX();
        } else {
            colIdx = getCellIdxByCellPos(p, true);
            startMovePoint = p;
        }
        mouseDown = true;

    }

    private int getCellIdxByBorderPos(Point p) {
        int colCount = body.getColumnCount();
        int checkPos = 0;
        for (int i = 0; i < colCount; i++) {

            CollectionColumn column = (CollectionColumn) body.getColumn(i);
            checkPos += getThisColumnWidth(column);
            if (checkPos - DELTA_X <= p.getX() && p.getX() <= checkPos + DELTA_X) {
                return i;
            }
        }
        return -1;
    }

    private int getCellIdxByCellPos(Point p, boolean startMove) {
        int colCount = body.getColumnCount();

        int leftPos = 0;
        int rightPos = 0;
        for (int i = 0; i < colCount; i++) {
            leftPos = rightPos;
            CollectionColumn column = (CollectionColumn) body.getColumn(i);
            rightPos += getThisColumnWidth(column);
            if (leftPos + DELTA_X <= p.getX() && p.getX() <= rightPos - DELTA_X) {
                return startMove || p.getX() <= (leftPos + rightPos) / 2 ? i : i + 1;
            }
        }
        return -1;
    }

    private void onMouseOverEvent(final MouseOverEvent e) {
        if (!mouseDown) {
            setCursor(e);
        }
    }

    private void setCursor(final MouseEvent e) {
        setCursor(getPoint(e));
    }

    private Point getPoint(final MouseEvent e) {
        int x = e.getClientX() - body.getAbsoluteLeft();
        int y = e.getClientY() - body.getAbsoluteTop();
        return new Point(x, y);
    }

    private boolean isEventInHeaderArea(MouseEvent e)    {
        double y = e.getClientY();
        if (body.getAbsoluteTop() < y && y < body.getAbsoluteTop()  + BusinessUniverseConstants.COLLECTION_HEADER_HEIGHT) {
            return true;
        };

        return false;
    }

    private void setCursor(Point p) {
        setCursor(getCellIdxByBorderPos(p) < 0 ? DEFAULT_CURSOR : RESIZE_CURSOR);
    }

    private void setCursor(com.google.gwt.dom.client.Style.Cursor cursor) {
        DOM.setStyleAttribute(body.getElement(), "cursor", cursor.getCssName());
    }

    private void onMouseUpEvent(final MouseUpEvent e) {

        if (mouseDown) {
            mouseDown = false;
            int x = e.getClientX() - body.getAbsoluteLeft();
            if (resizeStartPoint > 0) {
                resizeEndPoint = x;
                resizeColumn();
                resizeEndPoint = 0;
                resizeStartPoint = 0;
            } else if (startMovePoint != null && Math.abs(x - startMovePoint.getX()) > DELTA_DRAG) {
                e.stopPropagation();
                moveColumn(colIdx, getCellIdxByCellPos(getPoint(e), false));
                startMovePoint = null;
            }
        }

        this.setCursor(DEFAULT_CURSOR);
    }

    private void resizeColumn() {
        int move;
        CollectionColumn column = (CollectionColumn) body.getColumn(colIdx);
        if (CHECK_BOX_COLUMN_NAME.equalsIgnoreCase(column.getDataStoreName()) || !column.isResizable()) {
            return;
        }
        int width = getThisColumnWidth(column);
        if (colIdx != -1) {
            if (resizeStartPoint > resizeEndPoint) {
                move = width - (resizeStartPoint - resizeEndPoint);
                resizeModifier = BusinessUniverseUtils.adjustWidth(move, column.getMinWidth(), column.getMaxWidth());
                columnSizeCorrector();
            } else if (resizeStartPoint < resizeEndPoint) {
                move = width + (resizeEndPoint - resizeStartPoint);
                resizeModifier = (resizeEndPoint - resizeStartPoint) * (-1)/*move*(-1)*/;
                columnSizeCorrector();
            } else {
                move = width;
            }
            if (move > 0) {
                if (move > column.getMinWidth()) {
                    if (colIdx == body.getColumnCount() - 1) {
                        if (body.getOffsetWidth() > body.getOffsetWidth() - move) {
                            columnWindowResize(body.getParent().getParent()
                                    .getParent().getParent().getParent().getParent().getParent().getOffsetWidth() / body.getColumnCount());

                        }
                    } else {

                        body.setColumnWidth(column, move + "px");
                    }
                } else {
                    body.setColumnWidth(column, column.getMinWidth() + "px");

                }
            }
        }
        searchPanelResize();
    }


    private double getColumnWidth(CollectionColumn column) {
        String width = body.getColumnWidth(column);
        width = width.replaceAll("[^0-9\\.]", "");
        double columnIntWidth = Double.parseDouble(width);
        return columnIntWidth;
    }

    private void columnSizeCorrector() {
        CollectionColumn column = (CollectionColumn) body.getColumn(body.getColumnCount() - 1);
        double lastColumnWidth = getColumnWidth(column);
        double currentColumn = getColumnWidth((CollectionColumn) body.getColumn(colIdx));
        if (resizeModifier > 0) {
            double sum = (currentColumn - resizeModifier) + lastColumnWidth;
            body.setColumnWidth(column, sum + "px");
            resizeModifier = 0;

        } else if (resizeModifier < 0) {
            double lastColumntSize = (lastColumnWidth + resizeModifier);
            if (lastColumntSize < COLUMN_MIN_WIDTH) {
                lastColumntSize = COLUMN_MIN_WIDTH;
                body.setColumnWidth(column, lastColumntSize + "px");

            } else {
                body.setColumnWidth(column, lastColumntSize + "px");
            }
            resizeModifier = 0;

        }

    }

    private int getThisColumnWidth(Column column) {
        String colWidth = body.getColumnWidth(column);
        int checkPos = 0;
        colWidth = colWidth.replaceAll("[^0-9\\.]", "");
        try {
            checkPos += Math.round(Double.parseDouble(colWidth));
        } catch (NumberFormatException e) {
        }
        return checkPos;
    }

    private void onMouseMoveEvent(final MouseMoveEvent e) {
        mouseDown = (/*e.getButton() &*/ NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT;
        int x = e.getClientX() - body.getAbsoluteLeft();
        int y = e.getClientY() - body.getAbsoluteTop();
        if (mouseDown && startResizePoint != null
                && isDragDiff(x, y, startResizePoint.getX(), startResizePoint.getY())) {

            setCursor(RESIZE_CURSOR);

        } else if (mouseDown && startMovePoint != null
                && isDragDiff(x, y, startMovePoint.getX(), startMovePoint.getY())) {

            setCursor(MOVE_CURSOR);
        } else {
            setCursor(DEFAULT_CURSOR);
        }
    }

    private void moveColumn(int colIdx, int newColIdx) {
        if (colIdx != -1 && newColIdx != -1) {
            CollectionColumn column = (CollectionColumn) body.getColumn(colIdx);
            if (CHECK_BOX_COLUMN_NAME.equalsIgnoreCase(column.getDataStoreName())) {
                return;
            }

            body.removeColumn(colIdx);

            if (colIdx > newColIdx) {
                body.insertColumn(newColIdx, column, column.getDataStoreName());


            } else {
                Column columnToRemove = body.getColumn(newColIdx - 2);
                if (CHECK_BOX_COLUMN_NAME.equalsIgnoreCase(columnToRemove.getDataStoreName())) {
                    return;
                }
                body.insertColumn(newColIdx - 2, column, column.getDataStoreName());


            }

        }

    }

    private boolean isDragDiff(double x1, double y1, double x2, double y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) > DELTA_DRAG;


    }

    private void searchPanelResize() {


    }


}
