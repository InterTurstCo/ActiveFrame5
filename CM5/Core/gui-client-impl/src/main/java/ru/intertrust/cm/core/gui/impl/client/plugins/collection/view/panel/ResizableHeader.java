package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Event;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 13.09.13
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class ResizableHeader<T> extends Header<String> {
    //TODO Have following strings localized from separate properties file

    private static final String MOVE_tt = "Click and drag to move column";
    private static final String RESIZE_tt = "Подсказка для расширения границ колонки ";
    private static final Style.Cursor moveCursor = Style.Cursor.MOVE;
    private static final Style.Cursor resizeCursor = Style.Cursor.COL_RESIZE;
    private static final String RESIZE_COLOR = "#A49AED";
    private static final String MOVE_COLOR = "gray";
    private static final int MINIMUM_COLUMN_WIDTH = 80;
    private String title;
    private final Document document = Document.get();
    private final AbstractCellTable<T> tableHeader;
    private final AbstractCellTable<T> tableBody;
    private HeaderHelper current;
    protected final Column<T, ?> column;
    private final String resizeStyle;




    public ResizableHeader(String title, AbstractCellTable<T> tableHeader, AbstractCellTable<T> tableBody, Column<T, ?> column) {
        this(title, tableHeader,tableBody, column, null);
    }


    public ResizableHeader(String title, AbstractCellTable<T> tableHeader, AbstractCellTable<T> tableBody, Column<T, ?> column,
                           String resizeStyle) {
        super(new HeaderCell());
        if (title == null || tableHeader == null || column == null)
            throw new NullPointerException();
        this.title = title;
        this.column = column;
        this.tableHeader = tableHeader;
        this.tableBody = tableBody;
        this.resizeStyle = resizeStyle;

    }



    @Override
    public String getValue() {
        return title;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element target, NativeEvent event) {
        if (current == null)
            current = new HeaderHelper(target, event);
    }

    interface IDragCallback {
        void dragFinished();
    }

    private static final int RESIZE_HANDLE_WIDTH = 10;

    private static NativeEvent getEventAndPreventPropagation(Event.NativePreviewEvent event) {
        final NativeEvent nativeEvent = event.getNativeEvent();
        nativeEvent.preventDefault();
        nativeEvent.stopPropagation();
        return nativeEvent;
    }

    private static void setLine(Style style, int width, int top, int height, String color) {
        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(top, PX);
        style.setHeight(height, PX);
        style.setWidth(width, PX);
        style.setBackgroundColor(color);
    }

    private class HeaderHelper implements Event.NativePreviewHandler, IDragCallback {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final Element source;
        private boolean dragging;
        final Element mover, left, right;

        public HeaderHelper(Element target, NativeEvent event) {
            this.source = target;
            System.out.println(source.getOffsetTop() + " " +source.getAbsoluteTop());
            event.preventDefault();
            event.stopPropagation();
            mover = document.createDivElement();
            final int leftBound = target.getOffsetLeft() + target.getOffsetWidth();
            left = createSpanElement( MOVE_tt, MOVE_COLOR, moveCursor, leftBound - 2 * RESIZE_HANDLE_WIDTH);
            if (resizeStyle != null) {
                right = createSpanElement(resizeStyle, /*resizeToolTip*/"", leftBound - RESIZE_HANDLE_WIDTH);
            }else {
                right = createSpanElement( RESIZE_tt, RESIZE_COLOR, resizeCursor, leftBound - RESIZE_HANDLE_WIDTH);
            }
            mover.appendChild(left);
            mover.appendChild(right);
            source.appendChild(mover);
        }

        private SpanElement createSpanElement(String styleClassName, String title, double left){
            final SpanElement span = document.createSpanElement();
            span.setClassName(styleClassName);
            if (title != null) {
                span.setTitle(title);
            }
            final Style style = span.getStyle();
            style.setPosition(Style.Position.ABSOLUTE);
            style.setBottom(0, PX);
            style.setHeight(source.getOffsetHeight(), PX);
            style.setTop(source.getOffsetTop(), PX);
            style.setWidth(RESIZE_HANDLE_WIDTH, PX);
            style.setLeft(left, PX);
            return span;
        }

        private SpanElement createSpanElement( String title, String backgroundColor, Style.Cursor cursor, double left){
            final SpanElement span = document.createSpanElement();
            span.setAttribute("title", title);
            final Style style = span.getStyle();
            style.setCursor(cursor);
            style.setPosition(Style.Position.ABSOLUTE);
            style.setBottom(0, PX);
            style.setHeight(source.getOffsetHeight(), PX);
            style.setTop(source.getOffsetTop(), PX);
            style.setWidth(RESIZE_HANDLE_WIDTH, PX);
            style.setLeft(left, PX);

            return span;
        }

        @Override
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            final NativeEvent natEvent = event.getNativeEvent();
            final Element element = natEvent.getEventTarget().cast();
            final String eventType = natEvent.getType();
            if (!(element == left || element == right)) {
                if ("mousedown".equals(eventType)) {
                    //No need to do anything, the event will be passed on to the column sort handler
                } else if (!dragging && "mouseover".equals(eventType)) {
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
    }

    private class ColumnResizeHelper implements Event.NativePreviewHandler {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement resizeLine = document.createDivElement();
        private final Style resizeLineStyle = resizeLine.getStyle();
        private final Style resizeLineStyleBody = resizeLine.getStyle();
        private final Element header;
        private final IDragCallback dragCallback;
        private final Element caret;

        private ColumnResizeHelper(IDragCallback dragCallback, Element header, Element caret, NativeEvent event) {
            this.dragCallback = dragCallback;
            this.header = header;
            this.caret = caret;
            setLine(resizeLineStyle, 2, header.getAbsoluteTop() + header.getOffsetHeight(), getTableBodyHeight(), RESIZE_COLOR);
            moveLine(event.getClientX());

        }

        @Override
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            final int clientX = nativeEvent.getClientX();
            final String eventType = nativeEvent.getType();
            if ("mousemove".equals(eventType)) {
                moveLine(clientX);
            } else if ("mouseup".equals(eventType)) {
                handler.removeHandler();
                resizeLine.removeFromParent();
                dragCallback.dragFinished();
                columnResized(Math.max(clientX - header.getAbsoluteLeft(), MINIMUM_COLUMN_WIDTH)+"");
            }
        }

        private void moveLine(final int clientX) {
            final int xPos = clientX - tableHeader.getAbsoluteLeft();
            final int xPos2 = clientX - tableBody.getAbsoluteLeft();
            caret.getStyle().setLeft(xPos - caret.getOffsetWidth() / 2, PX);
            resizeLineStyle.setLeft(xPos, PX);
            caret.getStyle().setLeft(xPos2 - caret.getOffsetWidth() / 2, PX);
            resizeLineStyle.setLeft(xPos2, PX);
            resizeLineStyle.setTop(header.getOffsetHeight(), PX);
        }
    }

    private class ColumnMoverHelper implements Event.NativePreviewHandler {
        private static final int ghostLineWidth = 4;
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement ghostLine = document.createDivElement();
        private final Style ghostLineStyle = ghostLine.getStyle();
        private final DivElement ghostColumn = document.createDivElement();
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
            final int bodyHeight = getTableBodyHeight();
            setLine(ghostLineStyle, ghostLineWidth, top, bodyHeight, RESIZE_COLOR);


        }

        @Override
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {

        }


    }

    private static class HeaderCell extends AbstractCell<String> {
        public HeaderCell() {
            super("mousemove");
        }

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.append(SafeHtmlUtils.fromString(value));
        }
    }

    protected void columnResized(String newWidth) {
        tableHeader.setColumnWidth(column, newWidth + "px");
        tableBody.setColumnWidth(column, newWidth + "px");
    }




    protected abstract int getTableBodyHeight();
};