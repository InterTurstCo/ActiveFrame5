package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/14
 *         Time: 12:05 PM
 */

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionParameterizedColumn;

import java.util.Date;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

public class CollectionColumnHeader extends Header<HeaderWidget> {
    private static final String MOVE = "";
    private static final String RESIZE = "";
    private static final String MOVE_tt = "Click and drag to move column";
    private static final String RESIZE_tt = "Click and drag to resize column";
    private static final Style.Cursor moveCursor = Cursor.MOVE;
    private static final Style.Cursor resizeCursor = Cursor.COL_RESIZE;
    private static final String RESIZE_COLOR = "gray";
    private static final String MOVE_COLOR = "#A2ADBF";
    private static final String FOREGROUND_COLOR = "white";
    private static final double GHOST_OPACITY = .2;

    private final Document document = Document.get();
    private final CollectionDataGrid table;
    private final Element tableElement;
    private HeaderHelper current;
    protected final CollectionColumn column;
    private Element input;
    private HeaderWidget widget;
    private static final int RESIZE_HANDLE_WIDTH = 34;
    private String searchAreaId;
    private EventBus eventBus;
    private Element clearButton;
    private Element datePickerOpen;
    private InputElement inputFilter;
    private boolean focused;


    public CollectionColumnHeader(CollectionDataGrid table, CollectionColumn column, HeaderWidget headerWidget, EventBus eventBus) {
        super(new HeaderCell());
        this.column = column;
        this.table = table;
        this.tableElement = table.getElement();
        this.widget = headerWidget;
        this.eventBus = eventBus;
        searchAreaId = headerWidget.getId();

    }
    public DateTimeFormat getDateTimeFormat() {
       return widget.getDateTimeFormat();
    }

    public String getFilterValue() {
        if (widget.getSearchFilterName() !=  null) {
        return inputFilter.getValue();
        } else {
            return EMPTY_VALUE;

        }
    }

    public void resetFilterValue() {
        if (widget.getSearchFilterName() !=  null) {
        inputFilter.setValue(EMPTY_VALUE);
        }

    }

    public void setFocus() {
        if (focused) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    input = DOM.getElementById(searchAreaId + HEADER_INPUT_ID_PART);
                    input.focus();
                }
            });

            focused = false;

        }
    }

    public void updateFilterValue() {
        if (widget.getSearchFilterName() !=  null) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                input = DOM.getElementById(searchAreaId + HEADER_INPUT_ID_PART);
                inputFilter = InputElement.as(input);
                inputFilter.setValue(widget.getFilterValue());

            }
        });
        }
    }
    public void saveFilterValue(){
        if (widget.getSearchFilterName() !=  null) {
       widget.setFilterValue(inputFilter.getValue());
        }
    }

    public void initElements() {
        clearButton = DOM.getElementById(searchAreaId + HEADER_CLEAR_BUTTON_ID_PART);
        Element input = DOM.getElementById(searchAreaId + HEADER_INPUT_ID_PART);
        inputFilter = InputElement.as(input);
        if (widget.getDateBox() != null) {
            datePickerOpen = DOM.getElementById(searchAreaId + HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART);

        }
    }

    public void setSearchAreaVisibility(boolean visibility) {
        if (widget.getSearchFilterName() == null) {
            return;
        }
        widget.setShowFilter(visibility);

        if (visibility) {
            DOM.getElementById(searchAreaId).getStyle().clearDisplay();
        } else {
            DOM.getElementById(searchAreaId).getStyle().setDisplay(Style.Display.NONE);
        }
    }

    @Override
    public HeaderWidget getValue() {
        return widget;
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        if (widget.getSearchFilterName() != null) {
        initElements();
        }
        String eventType = event.getType();
        if (event.getKeyCode() == KeyCodes.KEY_ENTER && eventType.equalsIgnoreCase("keydown") && widget.getSearchFilterName() != null) {
            widget.setFilterValue(inputFilter.getValue());
            focused = true;
            eventBus.fireEvent(new FilterEvent(false));

            event.stopPropagation();
            event.preventDefault();

            return;
        }
        if (event.getKeyCode() == KeyCodes.KEY_ESCAPE && eventType.equalsIgnoreCase("keydown") && widget.getSearchFilterName() != null) {
            widget.setFilterValue(EMPTY_VALUE);
            eventBus.fireEvent(new FilterEvent(true));
            focused = false;
            event.stopPropagation();
            event.preventDefault();
            return;
        }
        if (current == null)
            current = new HeaderHelper(target, event);
    }

    public boolean onPreviewColumnSortEvent(Context context, Element elem, NativeEvent event) {
        if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
            return false;
        }
        return true;
    }

    interface IDragCallback {
        void dragFinished();
    }

    public String getFilterName() {
        return widget.getSearchFilterName();
    }

    public String getFieldType() {
        return widget.getFieldType();
    }

    public Date getDate() {
        return widget.getDateBox().getValue();
    }

    private static NativeEvent getEventAndPreventPropagation(NativePreviewEvent event) {
        final NativeEvent nativeEvent = event.getNativeEvent();
        nativeEvent.preventDefault();
        nativeEvent.stopPropagation();
        return nativeEvent;
    }

    private static void setLine(Style style, int width, int top, int height, String color) {
        style.setPosition(Position.ABSOLUTE);
        style.setTop(top, PX);
        style.setHeight(height, PX);
        style.setWidth(width, PX);
        style.setBackgroundColor(color);

    }

    private Element getClickedChild(NativeEvent event, String id) {
        if (widget.getSearchFilterName() == null) {
            return null;
        }
        Element target = Element.as(event.getEventTarget());
        NodeList<Element> buttons = Document.get().getElementById(id).getElementsByTagName("button");
        for (int i = 0; i < buttons.getLength(); i++) {
            if (buttons.getItem(i).isOrHasChild(target)) {
                return buttons.getItem(i);
            }
        }
        NodeList<Element> inputs = Document.get().getElementById(id).getElementsByTagName("input");
        for (int i = 0; i < inputs.getLength(); i++) {
            if (inputs.getItem(i).isOrHasChild(target)) {
                return inputs.getItem(i);
            }
        }

        return null;
    }

    private class HeaderHelper implements NativePreviewHandler, IDragCallback {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final Element source;
        private boolean dragging;
        private Element mover, left, right;

        public HeaderHelper(Element target, NativeEvent event) {
            this.source = target;
            event.preventDefault();
            event.stopPropagation();
            mover = document.createDivElement();
            final int leftBound = target.getOffsetLeft() + target.getOffsetWidth();
            if (column.isMoveable()) {
            left = createSpanElement(MOVE, MOVE_tt, MOVE_COLOR, moveCursor, leftBound - 2 * RESIZE_HANDLE_WIDTH);
            mover.appendChild(left);
            }
            if (column.isResizable()) {
                right = createSpanElement(RESIZE, RESIZE_tt, null, resizeCursor, leftBound - RESIZE_HANDLE_WIDTH);
                mover.appendChild(right);
            }
            source.appendChild(mover);
        }


        private SpanElement createSpanElement(String innerText, String title, String backgroundColor, Cursor cursor, double left) {
            final SpanElement span = document.createSpanElement();
            span.setInnerText(innerText);
            span.setAttribute("title", title);
            final Style style = span.getStyle();
            style.setCursor(cursor);
            style.setPosition(Position.ABSOLUTE);
            style.setBottom(0, PX);
            style.setHeight(source.getOffsetHeight(), PX);
            style.setTop(source.getOffsetTop(), PX);
            style.setColor(FOREGROUND_COLOR);
            style.setWidth(RESIZE_HANDLE_WIDTH, PX);
            style.setLeft(left, PX);
            if (backgroundColor != null) {
                style.setBackgroundColor(backgroundColor);
            }
            span.addClassName("header-cell-move-resize-border");
            return span;
        }

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            final NativeEvent natEvent = event.getNativeEvent();
            final Element element = natEvent.getEventTarget().cast();
            final String eventType = natEvent.getType();

            Element clickedElement = getClickedChild(natEvent, searchAreaId);
            if (eventType.equalsIgnoreCase("click")) {
                if (clickedElement != null) {
                    onHeaderElementClick(clickedElement);
                    natEvent.stopPropagation();
                    natEvent.preventDefault();
                    return;
                } else if(widget.getSearchFilterName() != null){
                    String filterValue = getFilterValue();
                    widget.setFilterValue(filterValue);
                }

            }
            if (eventType.equalsIgnoreCase("keydown")) {
                if (clickedElement != null) {
                    onHeaderElementClick(clickedElement);
                   /* natEvent.stopPropagation();
                    natEvent.preventDefault();*/
                }

                return;
            }
            if (!(element == left || element == right)) {
                if ("mousedown".equals(eventType)) {

                } else if (!dragging && "mouseover".equals(eventType)) {
                    if (clickedElement != null) {
                        if ((searchAreaId + HEADER_INPUT_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                            if (inputFilter.getValue().length() > 0) {
                                clearButton.setClassName("search-box-clear-button-on");
                            }
                            natEvent.stopPropagation();
                            natEvent.preventDefault();
                        }

                    }
                    cleanUp();
                }
                return;
            }
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            if ("mousedown".equals(eventType)) {
                if (element == right) {
                    left.removeFromParent();
                    new ColumnResizeHelper(this, source, right, nativeEvent);
                } else
                    new ColumnMoverHelper(this, source, nativeEvent);
                dragging = true;
            }
        }

        private void cleanUp() {
            handler.removeHandler();
            mover.removeFromParent();
            current = null;
        }

        public void dragFinished() {
            dragging = false;
            cleanUp();
        }

        private void onHeaderElementClick(Element clickedElement) {

            if ((searchAreaId + HEADER_INPUT_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                String filterValue =  inputFilter.getValue();
                if (filterValue.length() > 0) {

                    clearButton.setClassName("search-box-clear-button-on");
                }
            } else if ((searchAreaId + HEADER_CLEAR_BUTTON_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                inputFilter.setValue(EMPTY_VALUE);
                clearButton.setClassName("search-box-clear-button-off");
                inputFilter.focus();
            } else if ((searchAreaId + HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART).equalsIgnoreCase(clickedElement.getId())) {


                widget.getDateBox().getDatePicker().getElement().getStyle().setPosition(Position.ABSOLUTE);
                widget.getDateBox().getDatePicker().getElement().getStyle().setLeft(inputFilter.getAbsoluteLeft(), PX);
                widget.getDateBox().getDatePicker().getElement().getStyle().setTop(inputFilter.getAbsoluteTop(), PX);

                widget.getDateBox().showDatePicker( );
            }
        }

    }

    private class ColumnResizeHelper implements NativePreviewHandler {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement resizeLine = document.createDivElement();
        private final Style resizeLineStyle = resizeLine.getStyle();
        private final Element header;
        private final IDragCallback dragCallback;
        private final Element caret;

        private ColumnResizeHelper(IDragCallback dragCallback, Element header, Element caret, NativeEvent event) {
            this.dragCallback = dragCallback;
            this.header = header;
            this.caret = caret;
            setLine(resizeLineStyle, 1, header.getAbsoluteTop() + header.getOffsetHeight(), table.getOffsetHeight(), RESIZE_COLOR);
            moveLine(event.getClientX());
            tableElement.appendChild(resizeLine);
        }

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            final int clientX = nativeEvent.getClientX();
            final String eventType = nativeEvent.getType();
            if ("mousemove".equals(eventType)) {
                moveLine(clientX);
            } else if ("mouseup".equals(eventType)) {
                handler.removeHandler();
                resizeLine.removeFromParent();
                dragCallback.dragFinished();
                changeColumnWidth(Math.min(column.getMaxWidth(), Math.max(clientX - header.getAbsoluteLeft(), column.getMinWidth())));
            }
        }

        private void moveLine(final int clientX) {
            final int xPos = clientX - table.getAbsoluteLeft();
            caret.getStyle().setLeft(xPos - caret.getOffsetWidth() / 2, PX);
            resizeLineStyle.setLeft(xPos, PX);
            resizeLineStyle.setTop(header.getOffsetHeight(), PX);
        }
    }

    private class ColumnMoverHelper implements NativePreviewHandler {
        private static final int ghostLineWidth = 2;
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement ghostLine = document.createDivElement();
        private final Style ghostLineStyle = ghostLine.getStyle();
        private final DivElement ghostColumn = document.createDivElement();
        private final Style ghostColumnStyle = ghostColumn.getStyle();
        private final int columnWidth;
        private final int[] columnXPositions;
        private final IDragCallback dragCallback;
        private int fromIndex = -1;
        private int toIndex;

        private ColumnMoverHelper(IDragCallback dragCallback, Element target, NativeEvent event) {
            this.dragCallback = dragCallback;
            final int clientX = event.getClientX();
            columnWidth = target.getOffsetWidth();
            final Element tr = target.getParentElement();
            final int columns = tr.getChildCount();
            columnXPositions = new int[columns + 1];
            columnXPositions[0] = tr.getAbsoluteLeft();
            for (int i = 0; i < columns; ++i) {
                final int xPos = columnXPositions[i] + ((Element) tr.getChild(i)).getOffsetWidth();
                if (xPos > clientX && fromIndex == -1)
                    fromIndex = i;
                columnXPositions[i + 1] = xPos;
            }
            toIndex = fromIndex;
            final int top = target.getOffsetHeight();
            final int bodyHeight = table.getOffsetHeight();
            setLine(ghostColumnStyle, columnWidth, top, bodyHeight, MOVE_COLOR);
            setLine(ghostLineStyle, ghostLineWidth, top, bodyHeight, RESIZE_COLOR);
            ghostColumnStyle.setOpacity(GHOST_OPACITY);
            moveColumn(clientX);
            tableElement.appendChild(ghostColumn);
            tableElement.appendChild(ghostLine);
        }

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            final String eventType = nativeEvent.getType();
            if ("mousemove".equals(eventType)) {
                moveColumn(nativeEvent.getClientX());
            } else if ("mouseup".equals(eventType)) {
                handler.removeHandler();
                ghostColumn.removeFromParent();
                ghostLine.removeFromParent();
                if (fromIndex != toIndex)
                    columnMoved(fromIndex, toIndex);
                dragCallback.dragFinished();
            }
        }

        private void moveColumn(final int clientX) {
            final int pointer = (int) (clientX - columnWidth + 1.5 * RESIZE_HANDLE_WIDTH);
            ghostColumnStyle.setLeft(pointer - table.getAbsoluteLeft(), PX);
            for (int i = 0; i < columnXPositions.length - 1; ++i) {
                if (clientX < columnXPositions[i + 1]) {
                    final int adjustedIndex = i > fromIndex ? i + 1 : i;
                    int lineXPos = columnXPositions[adjustedIndex] - table.getAbsoluteLeft();
                    if (adjustedIndex == columnXPositions.length - 1) {//last columns
                        lineXPos -= ghostLineWidth;
                    } else if (adjustedIndex > 0) {
                        lineXPos -= ghostLineWidth / 2;
                    }
                    ghostLineStyle.setLeft(lineXPos, PX);
                    toIndex = i;
                    break;
                }
            }
        }
    }

    protected void changeColumnWidth(int newWidth) {
        table.setColumnWidth(column, newWidth + "px");

        table.redraw();
    }

    protected void columnMoved(int fromIndex, int toIndex) {
        CollectionColumn toReplace = (CollectionColumn) table.getColumn(fromIndex);
        if (toReplace instanceof CollectionParameterizedColumn) {
        table.removeColumn(fromIndex);
        table.insertColumn(toIndex, column, this);
        }
    }


}