package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

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
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentOrderChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentWidthChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.DatePickerPopup;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.DateFilterHeaderWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidget;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.MOVE_COLUMN_HINT_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.RESIZE_COLUMN_HINT_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

public class CollectionColumnHeader extends Header<HeaderWidget> {
    private static final String MOVE = "";
    private static final String RESIZE = "";
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
    private HeaderWidget widget;

    private String widgetId;
    private EventBus eventBus;
    private Element clearButton;
    private InputElement inputFilter;

    public CollectionColumnHeader(CollectionDataGrid table, CollectionColumn column, HeaderWidget headerWidget, EventBus eventBus) {
        super(new HeaderCell());
        this.column = column;
        this.table = table;
        this.tableElement = table.getElement();
        this.widget = headerWidget;
        headerWidget.setEventBus(eventBus);
        this.eventBus = eventBus;
        widgetId = headerWidget.getId();

    }

    public String getFilterValue() {
        if (widget.hasFilter()) {
            inputFilter = getInputFilter();
            if (inputFilter != null) {
                String value = inputFilter.getValue();
                widget.setFilterValuesRepresentation(value);
                return value;
            }
        }
        return null;
    }

    public HeaderWidget getHeaderWidget() {
        return widget;
    }


    public void resetFilterValue() {
        if (widget.hasFilter()) {
            widget.setFilterValuesRepresentation(EMPTY_VALUE);
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    inputFilter = getInputFilter();
                    if (inputFilter != null) {
                        inputFilter.setValue(EMPTY_VALUE);
                    }

                }
            });

        }

    }

    public void setFilterInputWidth(int width) {
        widget.setFilterInputWidth(width);
    }

    public void setFocus() {
        if (widget.isFocused()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    Element input = getHeaderElement();
                    if (input == null) {
                        return;
                    }
                    input.focus();

                }
            });

        }
        widget.setFocused(false);
    }

    public void updateFilterValue() {
        if (widget.hasFilter()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    inputFilter = getInputFilter();
                    if (inputFilter != null) {
                        inputFilter.setValue(widget.getFilterValuesRepresentation());
                    }

                }
            });
        }
    }

    public void saveFilterValue() {
        if (widget.hasFilter()) {
            inputFilter = getInputFilter();
            if (inputFilter == null) {
                return;
            }
            widget.setFilterValuesRepresentation(inputFilter.getValue());
        }
    }

    public com.google.gwt.user.client.Element getHeaderElement() {
        return DOM.getElementById(widgetId + HEADER_INPUT_ID_PART);
    }

    public InputElement getInputFilter() {
        Element input = getHeaderElement();
        if (input == null) {
            return null;
        }
        return InputElement.as(input);
    }

    private void initElements() {
        if (widget.hasFilter() && widget.isShowFilter()) {
            clearButton = DOM.getElementById(widgetId + HEADER_CLEAR_BUTTON_ID_PART);
            inputFilter = getInputFilter();
        }
    }

    public void setSearchAreaVisibility(final boolean visibility) {
        if (!widget.hasFilter()) {
            return;
        }
        widget.setShowFilter(visibility);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                final Element element = DOM.getElementById(widgetId);
                if (element != null) {
                    if (visibility) {
                        element.getStyle().clearDisplay();
                        updateFilterValue();
                    } else {
                        element.getStyle().setDisplay(Style.Display.NONE);
                    }
                }

            }
        });

    }

    public void hideClearButton() {
        if (widget.hasFilter()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    clearButton = DOM.getElementById(widgetId + HEADER_CLEAR_BUTTON_ID_PART);
                    clearButton.setClassName("search-box-clear-button-off");

                }
            });

        }
    }

    @Override
    public HeaderWidget getValue() {
        return widget;
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        if (!column.isVisible()) {
            return;  // there are no handled events for not visible column
        }
        initElements();
        String eventType = event.getType();
        if (event.getKeyCode() == KeyCodes.KEY_ENTER && eventType.equalsIgnoreCase("keydown") && widget.hasFilter()) {
            widget.setFilterValuesRepresentation(inputFilter.getValue());
            widget.setFocused(true);
            eventBus.fireEvent(new FilterEvent(false));

            event.stopPropagation();
            event.preventDefault();

            return;
        }
        if (event.getKeyCode() == KeyCodes.KEY_ESCAPE && eventType.equalsIgnoreCase("keydown") && widget.hasFilter()) {
            widget.setFilterValuesRepresentation(EMPTY_VALUE);
            eventBus.fireEvent(new FilterEvent(true));
            widget.setFocused(false);
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
        if (!widget.hasFilter()) {
            return null;
        }
        Element target = Element.as(event.getEventTarget());
        Element element = Document.get().getElementById(id);
        if (element == null) {
            return null;
        }
        NodeList<Element> buttons = element.getElementsByTagName("button");
        for (int i = 0; i < buttons.getLength(); i++) {
            if (buttons.getItem(i).isOrHasChild(target)) {
                return buttons.getItem(i);
            }
        }
        NodeList<Element> inputs = element.getElementsByTagName("input");
        for (int i = 0; i < inputs.getLength(); i++) {
            if (inputs.getItem(i).isOrHasChild(target)) {
                return inputs.getItem(i);
            }
        }
        NodeList<Element> divs = element.getElementsByTagName("div");
        for (int i = 0; i < divs.getLength(); i++) {
            if (divs.getItem(i).isOrHasChild(target)) {
                return divs.getItem(i);
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
                String hint = LocalizeUtil.get(MOVE_COLUMN_HINT_KEY, MOVE_COLUMN_HINT);
                left = createSpanElement(MOVE, hint, MOVE_COLOR, moveCursor, leftBound - 2 * RESIZE_HANDLE_WIDTH);
                mover.appendChild(left);
            }
            if (column.isResizable()) {
                String hint = LocalizeUtil.get(RESIZE_COLUMN_HINT_KEY, RESIZE_COLUMN_HINT);
                right = createSpanElement(RESIZE, hint, null, resizeCursor, leftBound - RESIZE_HANDLE_WIDTH);
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

            Element clickedElement = getClickedChild(natEvent, widgetId);
            if (eventType.equalsIgnoreCase("click")) {
                if (clickedElement != null) {
                    onHeaderElementClick(clickedElement);
                    natEvent.stopPropagation();
                    natEvent.preventDefault();
                    return;
                } else if (widget.hasFilter()) {
                    String filterValue = getFilterValue();
                    widget.setFilterValuesRepresentation(filterValue);
                }
            }
            if (eventType.equalsIgnoreCase("keydown")) {
                if (clickedElement != null) {
                    onHeaderKeyDown(clickedElement);
                }

                return;
            }
            if (!(element == left || element == right)) {
                if ("mousedown".equals(eventType)) {

                } else if (!dragging && "mouseover".equals(eventType)) {
                    if (clickedElement != null) {
                        if ((widgetId + HEADER_INPUT_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                            inputFilter = getInputFilter();
                            if (inputFilter != null && inputFilter.getValue().length() > 0) {
                                clearButton.setClassName(GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn());
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

        private void onHeaderKeyDown(Element clickedElement) {
            if ((widgetId + HEADER_INPUT_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        String filterValue = inputFilter.getValue();
                        clearButton = DOM.getElementById(widgetId + HEADER_CLEAR_BUTTON_ID_PART);
                        if (filterValue.length() > 0) {
                            clearButton.setClassName(GlobalThemesManager.getCurrentTheme().commonCss().filterBoxClearButtonOn());
                        } else {
                            clearButton.setClassName("search-box-clear-button-off");

                        }

                    }
                });
            }
        }

        private void onHeaderElementClick(Element clickedElement) {
            if ((widgetId + HEADER_CLEAR_BUTTON_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                clearButton = DOM.getElementById(widgetId + HEADER_CLEAR_BUTTON_ID_PART);
                clearButton.setClassName("search-box-clear-button-off");
                inputFilter.setValue(EMPTY_VALUE);
                widget.setFilterValuesRepresentation(EMPTY_VALUE);
                inputFilter.focus();
                eventBus.fireEvent(new FilterEvent(false));
                
            } else if ((widgetId + HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART).equalsIgnoreCase(clickedElement.getId())) {
                if (widget instanceof DateFilterHeaderWidget) {
                    DateFilterHeaderWidget dateFilterHeaderWidget = (DateFilterHeaderWidget) widget;
                    DatePickerPopup datePickerPopup = dateFilterHeaderWidget.getDateBox();
                    Style style = datePickerPopup.getElement().getStyle();
                    style.setPosition(Position.ABSOLUTE);
                    int absoluteLeft = inputFilter.getAbsoluteLeft();

                    int absoluteTop = inputFilter.getAbsoluteTop();
                    datePickerPopup.show();
                    style.setTop(absoluteTop + 30, PX);
                    style.setLeft(absoluteLeft, PX);
                    cleanUp();
                }

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
                changeColumnWidth(Math.min(column.getMaxWidth(), Math.max(clientX - header.getAbsoluteLeft(),
                        column.getMinWidth())));
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
                    columnMoved(toIndex);
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

    private void changeColumnWidth(final int newWidth) {
        if(table.getColumnWidth(column).equalsIgnoreCase(newWidth + "px")) {
            return;
        }
        widget.setFilterInputWidth(newWidth - FILTER_CONTAINER_MARGIN);
        eventBus.fireEvent(new ComponentWidthChangedEvent(column, newWidth));

    }

    private void columnMoved(int toIndex) {
        eventBus.fireEvent(new ComponentOrderChangedEvent(column, table.getColumn(toIndex)));
    }
}