package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEventHandler;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModelCallback;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.Date;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 17.10.2014 17:58.
 */
public class MonthPanel extends AbstractCalendarPanel implements CalendarScrollEventHandler, MouseWheelHandler,
        CalendarTodayEventHandler {
    private static int DATE_ITEM_HEIGHT = 100;

    private int containerOffset;
    private FlowPanel monthContainer;
    private Date cursorDate;
    private Timer resizeTimer;
    private ScrollTimer scrollTimer;
    private FlowPanel switchBtn;
    private FlowPanel detailPanel;
    private DetailDatePanel detailItem;

    public MonthPanel(final EventBus localEventBus, final CalendarTableModel tableModel, final CalendarConfig config) {
        super(localEventBus, tableModel, config);
        sinkEvents(Event.ONMOUSEWHEEL);
        handlers.add(addHandler(this, MouseWheelEvent.getType()));
        handlers.add(this.localEventBus.addHandler(CalendarTodayEvent.TYPE, this));
        monthContainer = new FlowPanel();
        add(monthContainer);
        detailPanel = new FlowPanel();
        detailPanel.setStyleName("monthDetailPanel");
        add(detailPanel);
        switchBtn = createSwitchBtn();
        detailPanel.add(switchBtn);
    }

    @Override
    public void scrollTo(final Widget source, final Date date) {
        if (this != source) {
            if (scrollTimer == null) {
                scrollTimer = new ScrollTimer(date);
            } else {
                scrollTimer.cancel();
                scrollTimer.setScrollDate(date);
            }
            scrollTimer.schedule(500);
        }
    }

    @Override
    public void goToToday() {
        currentDate = new Date();
        CalendarUtil.resetTime(currentDate);
        cursorDate = CalendarUtil.copyDate(currentDate);
        tableModel.setSelectedDate(currentDate);
        calendarConfig.addHistoryValue(UserSettingsHelper.CALENDAR_SELECTED_DATE, currentDate);
        initialize();
    }

    @Override
    public void onMouseWheel(MouseWheelEvent event) {
        final int delta = event.getDeltaY() * -5;
        changeOffset(delta);
        final WeekItem weekItem = (WeekItem) monthContainer.getWidget(0);
        final DateItem dateItem = (DateItem) weekItem.getWidget(0);
        final Date date = CalendarUtil.copyDate(dateItem.date);
        final int dateOffset =  containerOffset / (DATE_ITEM_HEIGHT / 7);
        CalendarUtil.addDaysToDate(date, 7 + dateOffset);
        cursorDate = date;
        localEventBus.fireEvent(new CalendarScrollEvent(this, date));
    }

    @Override
    public void onResize() {
        if (resizeTimer == null) {
            resizeTimer = new Timer() {

                @Override
                public void run() {
                    resizeTimer = null;
                    buildPresentation(getStartDate());
                }
            };
        } else {
            resizeTimer.cancel();
        }
        resizeTimer.schedule(500);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        cursorDate = CalendarUtil.copyDate(tableModel.getSelectedDate());
        localEventBus.addHandler(CalendarScrollEvent.TYPE, this);
        initialize();
    }

    private void initialize() {
        final Date date = CalendarUtil.copyDate(cursorDate);
        CalendarUtil.addDaysToDate(date, -(cursorDate.getDay() + 13));
        final int dayOffset = cursorDate.getDay();
        containerOffset = DATE_ITEM_HEIGHT - dayOffset * DATE_ITEM_HEIGHT / 7;
        monthContainer.getElement().getStyle().setMarginTop(-containerOffset, Style.Unit.PX);
        buildPresentation(date);
    }

    private void buildPresentation(final Date date) {
        monthContainer.clear();
        if (detailItem != null) {
            detailPanel.remove(detailItem);
        }
        setSwitchBtnStyle();
        final int height = getOffsetHeight();
        int weekCount = height / DATE_ITEM_HEIGHT + 2;
        int calendarWidth = getCalendarWidth();
        for (int index = 0; index < weekCount; index++) {
            monthContainer.add(new WeekItem(date, calendarWidth, calendarConfig.isShowWeekend()));
            if (!calendarConfig.isShowWeekend()) {
                CalendarUtil.addDaysToDate(date, 2);
            }
        }
        if (calendarConfig.isShowDetailPanel()) {
            detailItem = new DetailDatePanel(tableModel, getOffsetWidth() / 5, height);
            detailPanel.add(detailItem);
        }
    }

    private int getCalendarWidth() {
        int result = getOffsetWidth();
        if (calendarConfig.isShowDetailPanel()) {  // detail panel width 20%
            result -= result / 5;
        }
        return result;
    }

    private void changeOffset(int delta) {
        containerOffset -= delta;
        if (containerOffset < 0) {
            final Date startDate = CalendarUtil.copyDate(getStartDate());
            CalendarUtil.addDaysToDate(startDate, -7);
            monthContainer.remove(monthContainer.getWidgetCount() - 1);
            monthContainer.insert(new WeekItem(startDate, getCalendarWidth(), calendarConfig.isShowWeekend()), 0);
            containerOffset += DATE_ITEM_HEIGHT;
        } else if (containerOffset > DATE_ITEM_HEIGHT) {
            final WeekItem weekItem = (WeekItem) monthContainer.getWidget(monthContainer.getWidgetCount() - 1);
            final DateItem dateItem = (DateItem) weekItem.getWidget(0);
            final Date startDate = CalendarUtil.copyDate(dateItem.date);
            CalendarUtil.addDaysToDate(startDate, 7);
            monthContainer.remove(0);
            monthContainer.add(new WeekItem(startDate, getCalendarWidth(), calendarConfig.isShowWeekend()));
            containerOffset -= DATE_ITEM_HEIGHT;
        }
        monthContainer.getElement().getStyle().setMarginTop(-containerOffset, Style.Unit.PX);
    }

    private void setSwitchBtnStyle() {
        if (calendarConfig.isShowDetailPanel()) {
            switchBtn.getElement().setClassName("monthPanelOnSwitchButton");
        } else {
            switchBtn.getElement().setClassName("monthPanelOffSwitchButton");
        }
    }

    private FlowPanel createSwitchBtn() {
        final FlowPanel result = new FlowPanel();
        result.add(new InlineLabel("Задачи дня"));
        result.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                calendarConfig.setShowDetailPanel(!calendarConfig.isShowDetailPanel());
                buildPresentation(getStartDate());
            }
        }, ClickEvent.getType());
        result.sinkEvents(Event.ONCLICK);
        return result;
    }

    private Date getStartDate() {
        final WeekItem weekItem = (WeekItem) monthContainer.getWidget(0);
        final DateItem dateItem = (DateItem) weekItem.getWidget(0);
        return dateItem.date;
    }

    private class WeekItem extends FlowPanel {

        private WeekItem(final Date date, final int width, final boolean showWeekend) {
            final int dayContainersCount = showWeekend ? 6 : 5;
            final int dateWidth = width / dayContainersCount;
            for (int index = 0; index < dayContainersCount; index++) {
                if (index < 5) {
                    add(new DateItem(date, dateWidth, DATE_ITEM_HEIGHT));
                } else {
                    add(new WeekendItem(date, dateWidth, DATE_ITEM_HEIGHT));
                }
                CalendarUtil.addDaysToDate(date, 1);
            }
            getElement().getStyle().setFloat(Style.Float.NONE);
            getElement().getStyle().setWidth(width, Style.Unit.PX);
            getElement().getStyle().setHeight(DATE_ITEM_HEIGHT, Style.Unit.PX);
        }
    }

    private class WeekendItem extends AbstractWeekendItem {

        private WeekendItem(Date startDate, int width, int height) {
            super(startDate, width, height);
        }

        @Override
        protected AbstractDateItem createDateItem(Date date, int width, int height) {
            return new DateItem(date, width, height);
        }
    }

    private class DateItem extends AbstractDateItem {

        private DateItem(Date date, int width, int height) {
            super(date, width, height);
        }

        @Override
        protected void addStyles() {
            final int dateIndex = date.getDate();
            final int dayIndex = date.getDay();
            if (dayIndex < 6) {
                addStyleName("calendar-work-day-block");
            } else {
                addStyleName("calendar-holiday-block");
            }
            if (CalendarUtil.isSameDate(date, currentDate)) {
                addStyleName("today");
            }
            // build month border
            if (dateIndex < 8) {
                if (dateIndex == 1) {
                    final Label month = new Label(GuiUtil.MONTHS[date.getMonth()]);
                    month.setStyleName("first");
                    add(month);
                    if (dayIndex != 1) {
                        addStyleName("first-day-month");
                    } else {
                        addStyleName("days-of-first-week");
                    }
                } else if (dayIndex == 0) { // sunday
                    if (dateIndex == 2) {
                        addStyleName("second-weekend");
                    }
                } else if (dateIndex != 7 || dayIndex != 6) {
                    addStyleName("days-of-first-week");
                }
            }
        }

        @Override
        protected Label getItemLabel(Date date) {
            final Label result = new Label(Integer.toString(date.getDate()));
            result.setStyleName("calendarBlockDate");
            return result;
        }

        @Override
        protected Widget getTasksPanel() {
//            final Panel result = new AbsolutePanel();
            final Panel panel = new AbsolutePanel();
            panel.addStyleName("calendarTaskContainer");
            panel.getElement().getStyle().clearPosition();
            panel.getElement().getStyle().clearOverflow();
            final CalendarTableModelCallback callback = new CalendarTableModelCallbackImpl(panel);
            tableModel.fillByDateValues(date, callback);
//            result.add(panel);
//            return result;
            return panel;
        }
    }

    private static class CalendarTableModelCallbackImpl implements CalendarTableModelCallback {
        private final Panel container;

        private CalendarTableModelCallbackImpl(final Panel container) {
            this.container = container;
        }

        @Override
        public void fillValues(List<CalendarItemsData> values) {
            if (values != null) {
                for (CalendarItemsData calendarItemsData : values) {
                    if (calendarItemsData.getImage() != null) {
                        final Panel wrapper = new AbsolutePanel();
                        wrapper.setStyleName("calendarTaskWrapper");
                        final Image image = new Image(calendarItemsData.getImage());
                        image.setWidth(calendarItemsData.getImageWidth());
                        image.setHeight(calendarItemsData.getImageHeight());
                        wrapper.add(image);
                        wrapper.add(GuiUtil.getCalendarItemPresentation(
                               calendarItemsData.getMonthItem(), calendarItemsData.getRootObjectId()));
                        container.add(wrapper);
                    } else {
                        container.add(GuiUtil.getCalendarItemPresentation(
                                calendarItemsData.getMonthItem(), calendarItemsData.getRootObjectId()));
                    }
                }
            }
        }
    }

    private class ScrollTimer extends Timer {
        private Date scrollTo;

        private ScrollTimer(final Date scrollTo) {
            this.scrollTo = scrollTo;
        }

        @Override
        public void run() {
            scrollTimer = null;
            final int dayOffset = CalendarUtil.getDaysBetween(cursorDate, scrollTo);
            cursorDate = CalendarUtil.copyDate(scrollTo);
            if (Math.abs(dayOffset) > 7) {
                initialize();
            } else {
                final int delta = dayOffset * DATE_ITEM_HEIGHT / 7;
                changeOffset(-delta);
            }
        }

        public void setScrollDate(final Date scrollTo) {
            this.scrollTo = scrollTo;
        }
    }
}
