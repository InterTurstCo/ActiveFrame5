package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 30.09.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class TableController implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler  {

    private FlowPanel           tablePanelWinth;
    private int                 resizeModifier;

    static final int            COLUMN_MIN_WIDTH = 100;
    private Point               startResizePoint;
    private Point               startMovePoint;
    private int                 resizeStartPoint;
    private int                 resizeEndPoint;
    private boolean             mouseDown        = false;
    private int                 colIdx;
    private static int          DELTA_X          = 3;
    private static int          DELTA_DRAG       = 1;
    private static Style.Cursor MOVE_CURSOR      = Style.Cursor.MOVE;
    private static Style.Cursor RESIZE_CURSOR    = Style.Cursor.COL_RESIZE;
    private static Style.Cursor DEFAULT_CURSOR   = Style.Cursor.DEFAULT;

    CellTable<CollectionRowItem> header;
    CellTable<CollectionRowItem> body;

    public TableController(CellTable<CollectionRowItem> header, CellTable<CollectionRowItem> body) {
        this.header = header;
        this.body = body;


        header.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOVER);
        header.addHandler(this, MouseDownEvent.getType());
        header.addHandler(this, MouseOverEvent.getType());
        header.addHandler(this, MouseMoveEvent.getType());
        header.addHandler(this, MouseUpEvent.getType());


    }

    public void columnWindowResize(int width){
        int count = header.getColumnCount();
        for (int i = 0; i <count; i++ ){
            Column headCol = header.getColumn(i);
            Column bodyCol = body.getColumn(i);
            header.setColumnWidth(headCol, width+"px");
            body.setColumnWidth(bodyCol, width + "px");

        }

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

        onMouseOverEvent(event);
        event.preventDefault();
    }

    private void onMouseDownEvent(final MouseDownEvent e) {

        Point p = getPoint(e);
        colIdx = getCellIdxByBorderPos(p);
        if (colIdx >= 0){
            resizeStartPoint = (int) p.getX();
        }
        else {
            colIdx = getCellIdxByCellPos(p, true);
            startMovePoint = p;
        }
        mouseDown = true;

    }

    private int getCellIdxByBorderPos(Point p) {
        int colCount = header.getColumnCount();
        int checkPos = 0;
        for (int i = 0; i < colCount ; i++) {

            Column column = header.getColumn(i);
            checkPos += getThisColumnWidth(column);
            if (checkPos - DELTA_X <= p.getX()  && p.getX() <= checkPos + DELTA_X ) {
                return i;
            }
        }
        return -1;
    }

    private int getCellIdxByCellPos(Point p, boolean startMove) {
        int colCount = header.getColumnCount();

        int leftPos = 0;
        int rightPos = 0;
        for (int i = 0; i < colCount; i++) {
            leftPos = rightPos;
            Column column = header.getColumn(i);
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

    private Point getPoint(final MouseEvent e){
        int x = e.getClientX() - header.getAbsoluteLeft();
        int y = e.getClientX() - header.getAbsoluteTop();
        return new Point(x, y);
    }

    private void setCursor(Point p) {
        setCursor(getCellIdxByBorderPos(p) < 0 ? DEFAULT_CURSOR : RESIZE_CURSOR);
    }

    private void setCursor(com.google.gwt.dom.client.Style.Cursor cursor) {
        DOM.setStyleAttribute(header.getElement(), "cursor", cursor.getCssName());
    }

    private void onMouseUpEvent(final MouseUpEvent e) {
        if (mouseDown) {
            mouseDown = false;
            int x = e.getClientX() - header.getAbsoluteLeft();
            if ( resizeStartPoint >0 ){
                resizeEndPoint = x ;
                resizeColumn();
                resizeEndPoint = 0;
                resizeStartPoint = 0;
            }
            else if (startMovePoint != null && Math.abs(x - startMovePoint.getX()) > DELTA_DRAG) {
                e.stopPropagation();
                moveColumn(colIdx, getCellIdxByCellPos(getPoint(e), false));
                startMovePoint  = null;
            }
        }

        this.setCursor(DEFAULT_CURSOR);
    }

    private void resizeColumn(){
        int move;
        Column column = header.getColumn(colIdx);

        int width = getThisColumnWidth(column);
        if ( colIdx != -1 ){
            if (resizeStartPoint > resizeEndPoint){
                move = width -(resizeStartPoint - resizeEndPoint);
                resizeModifier = (correctResizeMovePoint(move));
                columnSizeCorrector();
            }
            else if (resizeStartPoint < resizeEndPoint){
                move = width +(resizeEndPoint - resizeStartPoint);
                resizeModifier = (resizeEndPoint - resizeStartPoint)*(-1)/*move*(-1)*/;
                columnSizeCorrector();
            } else {
                move = width;
            }
            if ( move > 0){
                if (move > COLUMN_MIN_WIDTH) {
                    if (colIdx == header.getColumnCount()-1){
                        if (header.getOffsetWidth() > header.getOffsetWidth() - move){
                            columnWindowResize(body.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getOffsetWidth() / header.getColumnCount());

                        }
                    }
                    else {
                    header.setColumnWidth(column, move+"px");
                    body.setColumnWidth(column, move+"px");
                    }
                }
                else {

                    header.setColumnWidth(column, COLUMN_MIN_WIDTH+"px");
                    body.setColumnWidth(column, COLUMN_MIN_WIDTH+"px");

                }
            }
        }
    }

    private int correctResizeMovePoint(int move){
        if (move > COLUMN_MIN_WIDTH){
            return move;
        }  else {
            return move +(COLUMN_MIN_WIDTH - move);
        }
    }

    private int getColumnWidth(Column column){
        String width  = header.getColumnWidth(column);
        width = width.replaceAll("[^0-9\\.]", "");
        int columnIntWidth  = Integer.parseInt(width);
        return columnIntWidth;
    }

    private void columnSizeCorrector(){
        Column column = header.getColumn(header.getColumnCount()-1);
        int lastColumnWidth  = getColumnWidth(column);
        int currentColumn = getColumnWidth(header.getColumn(colIdx));
        if (resizeModifier >0){
            int sum = (currentColumn - resizeModifier)+ lastColumnWidth ;
            header.setColumnWidth(column, sum+"px");
            body.setColumnWidth(column, sum+"px");
            resizeModifier = 0;


        }
        else if (resizeModifier < 0) {
            int lastColumntSize = (lastColumnWidth + resizeModifier);
            if (lastColumntSize < COLUMN_MIN_WIDTH){
                lastColumntSize = COLUMN_MIN_WIDTH;
                header.setColumnWidth(column, lastColumntSize+"px");
                body.setColumnWidth(column, lastColumntSize+"px");

            }

            else {
                header.setColumnWidth(column, lastColumntSize+"px");
                body.setColumnWidth(column, lastColumntSize+"px");
            }
            resizeModifier = 0;

        }
    }


    private int getThisColumnWidth(Column column){
        String colWidth = header.getColumnWidth(column);
        int checkPos = 0;
        colWidth = colWidth.replaceAll("[^0-9\\.]", "");
        try {
            checkPos += Math.round(Double.parseDouble(colWidth));
        }
        catch (NumberFormatException e) {
        }
        return checkPos;
    }

    private void onMouseMoveEvent(final MouseMoveEvent e) {
        mouseDown = (/*e.getButton() &*/ NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT;
        int x = e.getClientX() - header.getAbsoluteLeft();
        int y = e.getClientX() - header.getAbsoluteTop();
        if (mouseDown && startResizePoint != null
                && isDragDiff(x, y, startResizePoint.getX(), startResizePoint.getY())) {

            setCursor(RESIZE_CURSOR);

        }
        else if (mouseDown && startMovePoint != null
                && isDragDiff(x, y, startMovePoint.getX(), startMovePoint.getY())) {

            setCursor(MOVE_CURSOR);
        }
        else {
            setCursor(e);
        }
    }

    private void moveColumn(int colIdx, int newColIdx) {
        if (colIdx != -1 && newColIdx != -1) {
            Column  column = header.getColumn(colIdx);
            header.removeColumn(colIdx);
            body.removeColumn(colIdx);
            if (colIdx > newColIdx){
                header.insertColumn(newColIdx, column, column.getDataStoreName());
                body.insertColumn(newColIdx, column);
            }
            else{
                header.insertColumn(newColIdx -1, column, column.getDataStoreName());
                body.insertColumn(newColIdx -1, column);

            }
        }
    }

    private boolean isDragDiff(double x1, double y1, double x2, double y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) > DELTA_DRAG;


    }

}
