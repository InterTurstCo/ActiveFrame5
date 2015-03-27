package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEventHandler;

import java.util.Date;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.MONTHS;

/**
 * @author Sergey.Okolot
 *         Created on 13.10.2014 17:13.
 */
public class MonthScrollPanel extends HorizontalPanel implements RequiresResize,
        CalendarScrollEventHandler, MouseDownHandler, CalendarTodayEventHandler {
    public static int MONTH_SCROLL_ITEM_WIDTH = 126;

    private final EventBus localEventBus;
    private HandlerRegistration eventPreviewHandler;
    private HandlerRegistration mouseDownHandler;
    private HandlerRegistration todayHandler;
    private int containerOffset;
    private Date cursorDate;

    public MonthScrollPanel(final EventBus localEventBus, final Date cursorDate) {
        this.localEventBus = localEventBus;
        this.cursorDate = cursorDate;
        setStyleName("month-panel");
        this.sinkEvents(Event.ONMOUSEDOWN);
    }

    @Override
    public void onResize() {
        initializeContainer();
    }

    @Override
    public void goToToday() {
        cursorDate = new Date();
        initializeContainer();
    }

    @Override
    public void scrollTo(final Widget source, final Date date) {
        if (this != source && !CalendarUtil.isSameDate(cursorDate, date)) {
            final int dateDistance = -CalendarUtil.getDaysBetween(cursorDate, date);
            cursorDate = CalendarUtil.copyDate(date);
            final int delta = MONTH_SCROLL_ITEM_WIDTH / 31 * dateDistance;
            moveBy(delta);
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == Event.BUTTON_LEFT) {
            eventPreviewHandler = Event.addNativePreviewHandler(new NativePreviewHandlerImpl(event.getClientX()));
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initializeContainer();
        addHandlers();
    }

    @Override
    protected void onDetach() {
        mouseDownHandler.removeHandler();
        todayHandler.removeHandler();
        super.onDetach();
    }

    private void initializeContainer() {
        clear();
        int itemCount = getParent().getOffsetWidth() / MONTH_SCROLL_ITEM_WIDTH + 2;
        final boolean evenItems = (itemCount % 2) == 0;
        containerOffset = itemCount * MONTH_SCROLL_ITEM_WIDTH - getParent().getOffsetWidth();
        if (!evenItems) {
            containerOffset -= MONTH_SCROLL_ITEM_WIDTH;
        }
        containerOffset /= 2;
        containerOffset += MONTH_SCROLL_ITEM_WIDTH * cursorDate.getDate() / 31;
        int startMonthIndex = cursorDate.getMonth() - itemCount / 2;
        if (containerOffset > MONTH_SCROLL_ITEM_WIDTH) {
            startMonthIndex ++;
            containerOffset -= MONTH_SCROLL_ITEM_WIDTH;
        }
        int startYear = cursorDate.getYear() + 1900;
        if (startMonthIndex < 0) {
            startMonthIndex += 12;
            startYear--;
        }
        MonthItem item = new MonthItem(startMonthIndex, startYear);
        add(item);
        for (int index = 1; index < itemCount; index++) {
            item = MonthItem.next(item);
            add(item);
        }
        getElement().getStyle().setMarginLeft(-containerOffset, Style.Unit.PX);
    }

    private class NativePreviewHandlerImpl implements Event.NativePreviewHandler {

        private int startX;

        private NativePreviewHandlerImpl(int startX) {
            this.startX = startX;
        }

        @Override
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            switch (event.getTypeInt()) {
                case Event.ONMOUSEMOVE:
                    final int delta = (event.getNativeEvent().getClientX() - startX);
                    startX = event.getNativeEvent().getClientX();
                    moveBy(delta);
                    int cursorPosition = getWidgetCount() / 2;
                    final boolean evenItems = (getWidgetCount() % 2) == 0;
                    if (evenItems) {
                        cursorPosition--;
                    }
                    final MonthItem item = (MonthItem) getWidget(cursorPosition);
                    cursorDate = new Date(item.getYear() - 1900, item.getMonthIndex(), 0);
                    int dateIndex = containerOffset * 31 / MONTH_SCROLL_ITEM_WIDTH;
                    CalendarUtil.addDaysToDate(cursorDate, evenItems ? dateIndex + 15 : dateIndex);
                    localEventBus.fireEvent(new CalendarScrollEvent(MonthScrollPanel.this, cursorDate));
                    break;
                case Event.ONMOUSEUP:
                    eventPreviewHandler.removeHandler();
                    break;
            }
            event.cancel();
        }
    }

    private void moveBy(int delta) {
        int offset = Math.abs(delta) % MONTH_SCROLL_ITEM_WIDTH;
        if (delta > 0) {
            containerOffset -= offset;
            if (containerOffset < 0) {
                containerOffset += MONTH_SCROLL_ITEM_WIDTH;
                final MonthItem item = MonthItem.previous((MonthItem) getWidget(0));
                remove(getWidgetCount() - 1);
                insert(item, 0);
            }
        } else {
            containerOffset += offset;
            if (containerOffset > MONTH_SCROLL_ITEM_WIDTH) {
                containerOffset -= MONTH_SCROLL_ITEM_WIDTH;
                final MonthItem item = MonthItem.next((MonthItem) getWidget(getWidgetCount() - 1));
                remove(0);
                add(item);
            }
        }
        getElement().getStyle().setMarginLeft(-containerOffset, Style.Unit.PX);
    }

    private static class MonthItem extends Label {

        private int monthIndex;
        private int year;


        private MonthItem(final int monthIndex, final int year) {
            super(LocalizeUtil.get(MONTHS[monthIndex]) + ", " + year);
            setStyleName("month-block");
            getElement().getStyle().setWidth(MONTH_SCROLL_ITEM_WIDTH, Style.Unit.PX);
            this.monthIndex = monthIndex;
            this.year = year;
        }

        public int getMonthIndex() {
            return monthIndex;
        }

        public int getYear() {
            return year;
        }

        public static MonthItem previous(MonthItem current) {
            final int monthIndex;
            final int year;
            if (current.monthIndex == 0) {
                monthIndex = 11;
                year = current.year - 1;
            } else {
                monthIndex = current.monthIndex - 1;
                year = current.year;
            }
            final MonthItem result = new MonthItem(monthIndex, year);
            return result;
        }

        public static MonthItem next(MonthItem current) {
            final int monthIndex;
            final int year;
            if (current.monthIndex == 11) {
                monthIndex = 0;
                year = current.year + 1;
            } else {
                monthIndex = current.monthIndex + 1;
                year = current.year;
            }
            final MonthItem result = new MonthItem(monthIndex, year);
            return result;
        }

        @Override
        public String toString() {
            return new StringBuilder(MonthItem.class.getSimpleName())
                    .append(": ").append(getText()).toString();
        }
    }

    private void addHandlers(){
        localEventBus.addHandler(CalendarScrollEvent.TYPE, this);
        todayHandler = localEventBus.addHandler(CalendarTodayEvent.TYPE, this);
        mouseDownHandler = addHandler(this, MouseDownEvent.getType());
    }
}
