package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * @author Sergey.Okolot
 *         Created on 13.10.2014 17:13.
 */
public class MonthScrollPanel extends HorizontalPanel implements RequiresResize {
    private static final String[] months = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август",
            "сентябрь", "октябрь", "ноябрь", "декабрь"};
    static int MONTH_ITEM_WIDTH = 126;

    private HandlerRegistration eventPreviewHandler;
    private int containerOffset = MONTH_ITEM_WIDTH;
    private int initialWidth;
    private Date selected;

    public MonthScrollPanel(final Date selected) {
        this.selected = selected;
        setStyleName("month-panel");
        this.sinkEvents(Event.ONMOUSEDOWN);
    }

    @Override
    public void onResize() {
        if (getParent().getOffsetWidth() != initialWidth) {
            initializeContainer();
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initializeContainer();
    }

    @Override
    public void onBrowserEvent(Event event) {
        event.stopPropagation();
        switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
                if (event.getButton() == Event.BUTTON_LEFT) {
                    eventPreviewHandler =
                            Event.addNativePreviewHandler(new NativePreviewHandlerImpl(event.getClientX()));
                }
                break;
            default:
                super.onBrowserEvent(event);
        }
    }

    private void initializeContainer() {
        clear();
        initialWidth = getParent().getOffsetWidth();
        int itemCount = initialWidth / MONTH_ITEM_WIDTH + 2;

        int startDayOfWeek = CalendarUtil.getStartingDayOfWeek();

        int startMonthIndex = selected.getMonth() - itemCount / 2;
        int startYear = selected.getYear() + 1900;
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
                    final MonthItem first = (MonthItem) getWidget(0);
                    final int itemCount = getWidgetCount();
                    int selectedMonthIndex = first.getMonthIndex() + itemCount / 2;
                    int selectedYear = first.getYear();
                    if (selectedMonthIndex > 12) {
                        selectedMonthIndex -= 12;
                        selectedYear++;
                    }
                    System.out.println("-------------------------> selected " + months[selectedMonthIndex] + ", " + selectedYear);
                    final int currentX = event.getNativeEvent().getClientX();
                    int offset = Math.abs(currentX - startX) % MONTH_ITEM_WIDTH;
                    if (currentX > startX) {
                        containerOffset -= offset;
                        if (containerOffset < 0) {
                            containerOffset += MONTH_ITEM_WIDTH;
                            final MonthItem item = MonthItem.previous((MonthItem) getWidget(0));
                            remove(getWidgetCount() - 1);
                            insert(item, 0);
                        }
                    } else {
                        containerOffset += offset;
                        if (containerOffset > MONTH_ITEM_WIDTH) {
                            containerOffset -= MONTH_ITEM_WIDTH;
                            final MonthItem item = MonthItem.next((MonthItem) getWidget(getWidgetCount() - 1));
                            remove(0);
                            add(item);
                        }
                    }
                    getElement().getStyle().setMarginLeft(-containerOffset, Style.Unit.PX);
                    startX = currentX;
                    break;
                case Event.ONMOUSEUP:
                    eventPreviewHandler.removeHandler();
                    break;
            }
            event.cancel();
        }
    }

    private static class MonthItem extends Label {

        private int monthIndex;
        private int year;


        private MonthItem(final int monthIndex, final int year) {
            super(months[monthIndex] + ", " + year);
            setStyleName("month-block");
            getElement().getStyle().setWidth(MONTH_ITEM_WIDTH, Style.Unit.PX);
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
}
