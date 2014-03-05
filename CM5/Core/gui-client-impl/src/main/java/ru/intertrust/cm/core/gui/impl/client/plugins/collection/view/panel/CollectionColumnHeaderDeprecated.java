package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Event;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 30/9/13
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderDeprecated extends Header<HeaderWidget> {
    //TODO Have following strings localized from separate properties file

    private static final String MOVE_tt = "Click and drag to move column";
    private static final String RESIZE_tt = "Подсказка для расширения границ колонки ";
    private static final Style.Cursor MOVE_CURSOR = Style.Cursor.MOVE;
    private static final Style.Cursor resizeCursor = Style.Cursor.COL_RESIZE;
    private static final String RESIZE_COLOR = "#ff0000";
    private static final String MOVE_COLOR = "#ff0000";

    private String title;
    private final Document document = Document.get();
    private static final int RESIZE_HANDLE_WIDTH = 10;
    private final CollectionDataGrid tableBody;
    private HeaderHelper current;
    protected final CollectionColumn column;

    private HeaderWidget widget;


    public CollectionColumnHeaderDeprecated(CollectionDataGrid tableBody, CollectionColumn column, HeaderWidget widget) {

        super(new HeaderCell());
        if (tableBody == null || column == null)
            throw new NullPointerException();

        this.column = column;
        this.title = column.getDataStoreName();
        this.tableBody = tableBody;
        this.widget = widget;
    }

    @Override
    public HeaderWidget getValue() {
        return widget;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element target, NativeEvent event) {
        if (current == null) {
            current = new HeaderHelper(target, event);

        }
    }

    interface IDragCallback {
        void dragFinished();
    }

    private static NativeEvent getEventAndPreventPropagation(Event.NativePreviewEvent event) {
        final NativeEvent nativeEvent = event.getNativeEvent();
        nativeEvent.preventDefault();
        nativeEvent.stopPropagation();
        return nativeEvent;
    }

    private static void setLine(Style style, int width, int top, int height, String color) {

        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(top, PX);
        style.setHeight(26, PX);
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

            event.preventDefault();
            event.stopPropagation();
            mover = document.createDivElement();
            final int leftBound = target.getOffsetLeft() + target.getOffsetWidth();
            left = createSpanElement(MOVE_tt, MOVE_COLOR, MOVE_CURSOR, leftBound - 3 * RESIZE_HANDLE_WIDTH);

            right = createSpanElement(RESIZE_tt, RESIZE_COLOR, resizeCursor, leftBound - RESIZE_HANDLE_WIDTH);

            mover.appendChild(left);
            mover.appendChild(right);
            source.appendChild(mover);
        }

        private SpanElement createSpanElement(String styleClassName, String title, double left) {
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

        private SpanElement createSpanElement(String title, String backgroundColor, Style.Cursor cursor, double left) {
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
                    new ColumnMoverHelper(this, source, left, nativeEvent);
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
            tableBody.getElement().appendChild(resizeLine);
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

                columnResized(Math.max(clientX - header.getAbsoluteLeft(), column.getMinWidth()));
            }
        }

        private void moveLine(final int clientX) {

            final int xPos = clientX - tableBody.getAbsoluteLeft();
            //   caret.getStyle().setLeft(xPos - caret.getOffsetWidth() / 2, PX);
            resizeLineStyle.setLeft(xPos + 5, PX);

            resizeLineStyle.setTop(header.getOffsetHeight(), PX);

        }
    }

    private class ColumnMoverHelper implements Event.NativePreviewHandler {
        private static final int ghostLineWidth = 4;
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement ghostLine = document.createDivElement();
        private final Style ghostLineStyle = ghostLine.getStyle();
        private final DivElement ghostColumn = document.createDivElement();
        private final IDragCallback dragCallback;

        private int toIndex;
        private Element table;

        private ColumnMoverHelper(IDragCallback dragCallback, Element target, Element caret, NativeEvent event) {
            this.dragCallback = dragCallback;


            final Element tr = target.getParentElement();
            table = tr;

            final int top = target.getOffsetHeight();
            final int bodyHeight = getTableBodyHeight();
            setLine(ghostLineStyle, ghostLineWidth, top, bodyHeight, MOVE_COLOR);

        }

        @Override
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {

            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            final int clientX = nativeEvent.getClientX();
            final String eventType = nativeEvent.getType();
            if ("mousemove".equals(eventType)) {
                //  moveLine(clientX);
            } else if ("mouseup".equals(eventType)) {
                handler.removeHandler();
                dragCallback.dragFinished();
                int toIndex = getIndexOfColumn(clientX);
                if (toIndex == -1) {
                    return;
                }
                CollectionColumn columnToReplace = (CollectionColumn) tableBody.getColumn(toIndex);
                if (columnToReplace.getDataStoreName().equalsIgnoreCase("")) {
                    return;
                }

                if (column.getDataStoreName().equalsIgnoreCase("")) {
                    return;
                }
                tableBody.removeColumn(column);
                tableBody.insertColumn(toIndex, column, CollectionColumnHeaderDeprecated.this);
                tableBody.redrawHeaders();

                //     ghostLine.removeFromParent();
                /*handler.removeHandler();
                dragCallback.dragFinished();*/
            }
        }

        private void moveLine(final int clientX) {

            final int xPos = clientX - tableBody.getAbsoluteLeft();
            //   caret.getStyle().setLeft(xPos - caret.getOffsetWidth() / 2, PX);
            ghostLineStyle.setLeft(xPos + 5, PX);

            ghostLineStyle.setTop(tableBody.getOffsetHeight(), PX);

        }

        private int getIndexOfColumn(int clientX) {
            final int columnsNumber = table.getChildCount();

            int width = table.getAbsoluteLeft();
            for (int i = 0; i < columnsNumber; ++i) {

                int columnWidth = ((Element) table.getChild(i)).getOffsetWidth();
                if (width < clientX && clientX < width + columnWidth) {
                    return i;
                }
                width += columnWidth;
            }
            return -1;
        }


    }

    private static class HeaderCell extends AbstractCell<HeaderWidget> {
        public HeaderCell() {
            super("mousemove");
        }

     /*   @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.append(SafeHtmlUtils.fromString(value));
        }*/

        @Override
        public void render(Context context, final HeaderWidget value, SafeHtmlBuilder sb) {
            sb.append(new SafeHtml() {
                @Override
                public String asString() {
                    return value.toString();
                }
            });
        }
    }

    protected void columnResized(int newWidth) {

        tableBody.setColumnWidth(column, newWidth + "px");
    }

    protected int getTableBodyHeight() {
        return tableBody.getOffsetHeight();
    }

}