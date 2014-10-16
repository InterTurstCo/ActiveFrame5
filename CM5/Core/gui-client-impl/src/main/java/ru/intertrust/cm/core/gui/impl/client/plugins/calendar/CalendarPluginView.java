package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;

import java.util.Date;

/**
 * Created by lvov on 03.04.14.
 */
public class CalendarPluginView extends PluginView {

    private CalendarViewModel<CalendarData> viewModel;
    private MonthScrollPanel monthScrollPanel;



    public static final int CALENDAR_ROW_COUNT = 20;
    public static final int DAY_BLOCK_HEIGHT = 90;
    private Container container;
    private ScrollPanel scrollPanel = new ScrollPanel();
    private AbsolutePanel calendarContainer = new AbsolutePanel();
    private EventBus eventBus;
    private CalendarDayView lastSelectedDay;
    private Date today = new Date();
    private int daysBefore;
    private int daysAfter;
    private DatePicker datePicker = new DatePicker();

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected CalendarPluginView(Plugin plugin) {
        super(plugin);
        eventBus = new SimpleEventBus();
        final CalendarPluginData initialData = plugin.getInitialData();
        viewModel = new CalendarViewModel<>(new DataProviderImpl());
        final Date selectedDate = initialData.getSelectedDate();
        CalendarUtil.resetTime(selectedDate);
        viewModel.getSelectionModel().setSelected(selectedDate, true);
    }

    @Override
    public IsWidget getViewWidget() {
        container = new Container();

        final SimplePanel cursor = new SimplePanel();
        cursor.setStyleName("current-month-block");
        cursor.getElement().getStyle().setWidth(MonthScrollPanel.MONTH_ITEM_WIDTH, Style.Unit.PX);
        container.add(cursor);

        monthScrollPanel = new MonthScrollPanel(viewModel.getSelectionModel().getSelectedObject());
        container.add(monthScrollPanel);

        scrollPanel.addStyleName("calendar-scroll-panel");
        calendarContainer.addStyleName("calendar-container");
        scrollPanel.add(calendarContainer);
        container.add(scrollPanel);

        createCalendarList();
        setEventHandler();
        Application.getInstance().hideLoadingIndicator();
        return container;
    }

    private void createCalendarList() {
        for (int row = 0; row < CALENDAR_ROW_COUNT; row++) {
            createNextDayCalendarList();
            createPrivDayCalendarList();
        }
    }

    private void createPrivDayCalendarList() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSize("100%", "100%");
        daysBefore = daysBefore - 7;
        calendarRowBuilder(horizontalPanel, daysBefore);
        calendarContainer.insert(horizontalPanel, 0);

    }

    private void createNextDayCalendarList() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSize("100%", "100%");
        calendarRowBuilder(horizontalPanel, daysAfter);
        daysAfter = daysAfter + 7;
        calendarContainer.add(horizontalPanel);

    }

    private void deleteCalendarRowBeforeToday() {
        daysBefore = daysBefore + 7;
        calendarContainer.remove(0);
        scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition() - DAY_BLOCK_HEIGHT);
    }

    private void deleteCalendarRowAfterToday() {
        daysAfter = daysAfter - 7;
        calendarContainer.remove(calendarContainer.getWidgetCount() - 1);
        scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition() + DAY_BLOCK_HEIGHT);

    }

    private void calendarRowBuilder(HorizontalPanel horizontalPanel, int dayModifierDate) {

        for (int column = 0; column < 6; column++) {

            if (column == 5) {
                Date saturday = (Date) datePicker.getFirstDate().clone();
                Date sunday = (Date) datePicker.getFirstDate().clone();
                CalendarUtil.addDaysToDate(saturday, dayModifierDate);
                dayModifierDate++;
                CalendarUtil.addDaysToDate(sunday, dayModifierDate);
                dayModifierDate++;
                CalendarHolidayView day = new CalendarHolidayView(saturday, sunday, eventBus, plugin);
                horizontalPanel.add(day.getContainer());

            } else {
                Date workDate = (Date) datePicker.getFirstDate().clone();
                CalendarUtil.addDaysToDate(workDate, dayModifierDate);
                dayModifierDate++;
                CalendarWorkingDayView day = new CalendarWorkingDayView(workDate, eventBus, plugin);
                horizontalPanel.add(day.getContainer());

            }
        }
    }

    private void setEventHandler() {
//        eventBus.addHandler(CalendarSelectDayEvent.TYPE, new CalendarSelectDayEventHandler() {
//            @Override
//            public void selectDay(CalendarSelectDayEvent event) {
//                if (lastSelectedDay != null) {
//                    lastSelectedDay.resetSelectionStyle();
//                }
//                lastSelectedDay = event.getCalendarDayView();
//
//            }
//        });
//
//        scrollPanel.addScrollHandler(new ScrollHandler() {
//            @Override
//            public void onScroll(ScrollEvent event) {
//                if (scrollPanel.getVerticalScrollPosition() + 500 >= scrollPanel.getMaximumVerticalScrollPosition()) {
//
//                    for (int i = 0; i < 4; i++) {
//                        createNextDayCalendarList();
//                        deleteCalendarRowBeforeToday();
//                    }
//                    if (scrollPanel.getVerticalScrollPosition() >= scrollPanel.getMaximumVerticalScrollPosition()) {
//                        scrollPanel.setVerticalScrollPosition(scrollPanel.getMaximumVerticalScrollPosition() - 100);
//                    }
//                }
//
//                if (scrollPanel.getVerticalScrollPosition() <= scrollPanel.getMinimumVerticalScrollPosition() + 500) {
//
//                    for (int i = 0; i < 4; i++) {
//                        createPrivDayCalendarList();
//                        deleteCalendarRowAfterToday();
//
//                    }
//                    if (scrollPanel.getVerticalScrollPosition() <= scrollPanel.getMinimumVerticalScrollPosition()) {
//                        scrollPanel.setVerticalScrollPosition(100);
//                    }
//                }
//
//            }
//        });

    }

    private static class Container extends FlowPanel implements RequiresResize {

        private HandlerRegistration resizeHandler;

        @Override
        protected void onLoad() {
            super.onLoad();
            resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    Container.this.onResize();
                }
            });
        }

        @Override
        protected void onDetach() {
            if (resizeHandler != null) {
                resizeHandler.removeHandler();
                resizeHandler = null;
            }
            super.onDetach();
        }

        @Override
        public void onResize() {
            for (Widget widget : getChildren()) {
                if (widget instanceof RequiresResize) {
                    final RequiresResize requiresResize = (RequiresResize) widget;
                    requiresResize.onResize();
                }
            }
        }
    }

    class MonthScrollLine implements IsWidget     {

        AbsolutePanel monthPanel;
        static final int MONTH_WIDTH = 124;
        int monthCount;
        int year = today.getYear() + 1900;
        int monthStart;
        int monthEnd;
        int leftMonthBlockCorrector;
        HorizontalPanel monthScrollContainer = new HorizontalPanel();

        boolean mousePress;


        MonthScrollLine(AbsolutePanel horizontalPanel) {
            this.monthPanel = horizontalPanel;

            monthPanel.add(monthScrollContainer);
            getMonthCount();
            createMonthLine();





        }



        void createMonthLine() {
            monthPanel.addStyleName("month-panel");
            monthPanel.setWidth(plugin.getOwner().getVisibleWidth() + (MonthScrollLine.MONTH_WIDTH * 2) + "px");
            for (int i = 0; i < monthCount; i++) {
                FlowPanel flowPanel = new FlowPanel();
                flowPanel.addStyleName("month-block");
                flowPanel.add(new HTML(getMonthAndYear(today.getMonth() + monthStart)));
                monthScrollContainer.add(flowPanel);
                monthStart++;


            }

        }

        void getMonthCount() {
            monthStart = 0;
            monthEnd = 0;
            int width = plugin.getOwner().getVisibleWidth();
            monthCount = (width / MONTH_WIDTH) + 3;
            monthStart -= monthCount / 2;
            leftMonthBlockCorrector = (width / 2) - (Math.abs(monthStart * MONTH_WIDTH) + MONTH_WIDTH / 2);
            monthPanel.getElement().getStyle().setLeft(leftMonthBlockCorrector, Style.Unit.PX);

        }

        String getMonthAndYear(int num) {
            int thisYear = year;
            int tmp;
            if (num < 0) {
                if (num < -12) {
                    tmp = Math.abs(num / 12);
                    thisYear = thisYear - tmp;
                    num = num + (12 * tmp);

                }
                thisYear = thisYear - 1;
                num = num + 12;
            } else if (num == 0) {
                num = 0;
            } else if (num > 11) {
                tmp = Math.abs(num / 12);
                thisYear = thisYear + tmp;
                num = num % 12;
            }
            String name = CalendarDayView.month.get(num) + ", " + thisYear;
            return name;
        }


        @Override
        public Widget asWidget() {
            return monthPanel;
        }





    }
}






