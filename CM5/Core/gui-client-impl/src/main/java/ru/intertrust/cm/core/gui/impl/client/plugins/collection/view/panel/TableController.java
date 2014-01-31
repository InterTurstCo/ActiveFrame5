package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.TableControllerSortEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.SortCollectionState;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 30.09.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class TableController implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler  {

    private int ownerWidth ;
    private int                 resizeModifier;
    private EventBus eventBus;
    static final int            COLUMN_MIN_WIDTH = 120;
    private Point               startResizePoint;
    private Point               startMovePoint;
    private int                 mouseDownSortPoint;
    private int                 mouseUpSortPoint;
    private int                 resizeStartPoint;
    private int                 resizeEndPoint;
    private boolean             mouseDown        = false;
    private boolean             sortDirection;
    private int                 colIdx;
    private static int          DELTA_X          = 3;
    private static int          DELTA_DRAG       = 1;
    private static Style.Cursor MOVE_CURSOR      = Style.Cursor.MOVE;
    private static Style.Cursor RESIZE_CURSOR    = Style.Cursor.COL_RESIZE;
    private static Style.Cursor DEFAULT_CURSOR   = Style.Cursor.DEFAULT;
    private Column lastColumn;
    private HorizontalPanel searchPanel;

    CellTable<CollectionRowItem> header;
    CellTable<CollectionRowItem> body;

    public TableController(CellTable<CollectionRowItem> header, CellTable<CollectionRowItem> body, EventBus eventBus,
                           HorizontalPanel searchPanel) {
        this.header = header;
        this.body = body;
        this.eventBus = eventBus;
        this.searchPanel = searchPanel;


        header.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONMOUSEOVER);
        header.addHandler(this, MouseDownEvent.getType());
        header.addHandler(this, MouseOverEvent.getType());
        header.addHandler(this, MouseMoveEvent.getType());
        header.addHandler(this, MouseUpEvent.getType());


    }
    // изменение ширины столбцов на ровные части
    public void columnWindowResize(int width){
        int count = header.getColumnCount();
        for (int i = 0; i <count; i++ ){
            Column headCol = header.getColumn(i);
            Column bodyCol = body.getColumn(i);
            header.setColumnWidth(headCol, width+"px");
            body.setColumnWidth(bodyCol, width + "px");

        }

        searchPanelResize();

    }
    
    //изменение ширины столбцов таблицы согласно пропорции
    public void columnWindowResizeOnPercentage(int width){
        double modulo = 0;
        double count = header.getColumnCount();
        double oldWidth = getOldTableWidth();
        double newWidth = 0;

        for (int i = 0; i <count; i++ ){
            Column headCol = header.getColumn(i);
            Column bodyCol = body.getColumn(i);
            //использование -5 это компенсация погрешности из результата округлений размеров в double
            newWidth += reducingColumnWidthInPercentage(oldWidth, width-5, headCol, bodyCol);
        }

        if (width > newWidth && newWidth >0){
            modulo += width -newWidth;

        } else {
            modulo += newWidth - width;
        }

        while(modulo> 0) {
                for (int i = 0; i < count; i++){
                    if (modulo <= 0){
                        break;
                    }
                    Column headCol = header.getColumn(i);
                    Column bodyCol = body.getColumn(i);
                    double columnWidth = getColumnWidth(headCol);
                    columnWidth+=2;
                    header.setColumnWidth(headCol,columnWidth +"px");
                    body.setColumnWidth(bodyCol, columnWidth + "px");
                    modulo -= 2;

                }
            }

        searchPanelResize();


    }

    private double reducingColumnWidthInPercentage(double oldWidth, double newWidth, Column headCol, Column bodyCol){
        double coeff = newWidth / oldWidth;
        double resized ;
        if (newWidth / header.getColumnCount() <COLUMN_MIN_WIDTH) {
            resized = COLUMN_MIN_WIDTH;
            return 0;
        } else {
            resized = (getColumnWidth(headCol) * coeff);

        if (resized < COLUMN_MIN_WIDTH) {
            resized = COLUMN_MIN_WIDTH;
        }
        }
        header.setColumnWidth(headCol, resized+"px");
        body.setColumnWidth(bodyCol, resized + "px");
        return resized;

    }

    private int getOldTableWidth(){
        int oldWidth = 0;
        for(int i =0; i < body.getColumnCount(); i++) {
            Column column = body.getColumn(i);
            oldWidth += getColumnWidth(column);
        }
        return oldWidth;
    }



    @Override
    public void onMouseDown(MouseDownEvent event) {
        checkFstSortClick(event);
        onMouseDownEvent(event);
        event.preventDefault();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        checkSndSortClick(event);
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

    private void checkFstSortClick(final MouseEvent e){
        Point p = getPoint(e);
        mouseDownSortPoint = (int) p.getX();
    }
    private void checkSndSortClick(final MouseEvent e){
        Point p = getPoint(e);
        mouseUpSortPoint = (int) p.getX();
        if (mouseDownSortPoint == mouseUpSortPoint){
            fireSortEvent();
        }
    }

    private void fireSortEvent(){
        CollectionColumn column = (CollectionColumn) getSortedColumn();

        String colDataStoreName = column.getDataStoreName().replaceAll("([↓↑])", "");


        if (lastColumn != null){
        if (!lastColumn.equals(column)){
            String rename = lastColumn.getDataStoreName().replaceAll("([↓↑])", "");
            lastColumn.setDataStoreName(rename);


            resetLastColumn();

            }
        }

        if (!sortDirection ){
            sortDirection = true;
        }  else {
            sortDirection = false;
        }
        eventBus.fireEvent(new TableControllerSortEvent(colDataStoreName, new SortCollectionState(
                70, 0, colDataStoreName, sortDirection ,true, column.getFieldName())));

           lastColumn = column;


    }

    private void resetLastColumn(){

        for (int i = 0 ; i < header.getColumnCount(); i++){
            if (lastColumn.equals(header.getColumn(i))){
                 reDrawColumn(i, lastColumn);
            }
        }
    }

    private Column getSortedColumn(){
        int checkPos = 0;

        for (int i = 0; i < header.getColumnCount(); i++ ){
            Column column = header.getColumn(i);
            checkPos += getThisColumnWidth(column);
            if (checkPos > mouseDownSortPoint){
                String colName = header.getColumn(i).getDataStoreName() ;
                if (!sortDirection)  {
                    colName = colName.replaceAll("([↓↑])", "");
                    column.setDataStoreName(colName+"↑");

                } else {
                    colName = colName.replaceAll("([↓↑])", "");
                    column.setDataStoreName(colName+"↓");
                }
                reDrawColumn(i, column);
                return column;
            }
        }

        return null;
    }

    private void reDrawColumn(int index, Column column){
        header.removeColumn(index);
        header.insertColumn(index, column, column.getDataStoreName());


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
                            columnWindowResize(body.getParent().getParent()
                                    .getParent().getParent().getParent().getParent().getParent().getOffsetWidth()
                                    / header.getColumnCount());

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
        searchPanelResize();
    }

    private int correctResizeMovePoint(int move){
        if (move > COLUMN_MIN_WIDTH){
            return move;
        }  else {
            return move +(COLUMN_MIN_WIDTH - move);
        }
    }

    private double getColumnWidth(Column column){
        String width  = header.getColumnWidth(column);
        width = width.replaceAll("[^0-9\\.]", "");
        double columnIntWidth  = Double.parseDouble(width);
        return columnIntWidth;
    }

    private void columnSizeCorrector(){
        Column column = header.getColumn(header.getColumnCount()-1);
        double lastColumnWidth  = getColumnWidth(column);
        double currentColumn = getColumnWidth(header.getColumn(colIdx));
        if (resizeModifier >0){
            double sum = (currentColumn - resizeModifier)+ lastColumnWidth ;
            header.setColumnWidth(column, sum+"px");
            body.setColumnWidth(column, sum+"px");
            resizeModifier = 0;


        }
        else if (resizeModifier < 0) {
            double lastColumntSize = (lastColumnWidth + resizeModifier);
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
            Widget search = searchPanel.getWidget(colIdx);
            header.removeColumn(colIdx);
            body.removeColumn(colIdx);
            searchPanel.remove(colIdx);
            if (colIdx > newColIdx){
                header.insertColumn(newColIdx, column, column.getDataStoreName());
                body.insertColumn(newColIdx, column);
                searchPanel.insert(search, newColIdx);

            }
            else{
                header.insertColumn(newColIdx -1, column, column.getDataStoreName());
                body.insertColumn(newColIdx -1, column);
                searchPanel.insert(search, newColIdx -1);

            }
        }

    }

    private boolean isDragDiff(double x1, double y1, double x2, double y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) > DELTA_DRAG;


    }

    private void searchPanelResize(){

        for (int i = 0; i < searchPanel.getWidgetCount(); i++){
            int nextPos = getThisColumnWidth(header.getColumn(i));

            searchPanel.getWidget(i).setWidth(getThisColumnWidth(header.getColumn(i))+"px");


        }
    }

    public int getOwnerWidth() {
        return ownerWidth;
    }

    public void setOwnerWidth(int ownerWidth) {
        this.ownerWidth = ownerWidth;
    }
}
