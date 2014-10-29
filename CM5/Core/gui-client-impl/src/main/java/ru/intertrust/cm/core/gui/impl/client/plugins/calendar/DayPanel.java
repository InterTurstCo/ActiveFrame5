package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import java.util.List;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarNextWeekEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarNextWeekEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarPreviousWeekEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarPreviousWeekEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarTodayEventHandler;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModelCallback;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;

/**
 * @author Sergey.Okolot
 *         Created on 17.10.2014 14:17.
 */
public class DayPanel extends FlowPanel implements RequiresResize, CalendarNextWeekEventHandler,
        CalendarPreviousWeekEventHandler, CalendarTodayEventHandler {

    private final CalendarTableModel tableModel;
    private final EventBus localEventBus;
    private final CalendarConfig calendarConfig;
    private boolean weekendPanelExpanded;
    private Date currentDate = new Date();
    private Date beginWeekDate;

    public DayPanel(final EventBus localEventBus, final CalendarTableModel tableModel, final CalendarConfig config) {
        this.tableModel = tableModel;
        this.localEventBus = localEventBus;
        this.calendarConfig = config;
        weekendPanelExpanded = config.isShowWeekend();
        setStyleName("calendar-scroll-panel");
        this.localEventBus.addHandler(CalendarTodayEvent.TYPE, this);
        this.localEventBus.addHandler(CalendarPreviousWeekEvent.TYPE, this);
        this.localEventBus.addHandler(CalendarNextWeekEvent.TYPE, this);
    }

    @Override
    public void onResize() {
        buildPresentation();
    }

    @Override
    public void goToNextWeek() {
        CalendarUtil.addDaysToDate(beginWeekDate, 7);
        buildPresentation();
    }

    @Override
    public void goToPreviousWeek() {
        CalendarUtil.addDaysToDate(beginWeekDate, -7);
        buildPresentation();
    }

    @Override
    public void goToToday() {
        beginWeekDate = CalendarUtil.copyDate(tableModel.getSelectedDate());
        final int dayToMonday = beginWeekDate.getDay() - 1;
        CalendarUtil.addDaysToDate(beginWeekDate, - dayToMonday);
        buildPresentation();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        goToToday();
    }

    private void buildPresentation() {
        clear();
        final Date cursorDate = CalendarUtil.copyDate(beginWeekDate);
        final int dayCount = weekendPanelExpanded ? 6 : 5;
        final int height = getOffsetHeight();
        final int width = getOffsetWidth() / dayCount;
        for (int index = 0; index < dayCount; index++) {
            if (index < 5) {
                add(new DateItem(cursorDate, width, height));
            } else { // fixme change style to show weekend selector
                add(new WeekendItem(cursorDate, width, height));
            }
            CalendarUtil.addDaysToDate(cursorDate, 1);
        }
    }

    private class WeekendItem extends FlowPanel {

        private WeekendItem(final Date startDate, int width, final int height) {
            width -= 2;
            getElement().getStyle().setFloat(Style.Float.LEFT);
            getElement().getStyle().setWidth(width - 2, Style.Unit.PX);
            getElement().getStyle().setHeight(height - 1, Style.Unit.PX);
            final int childrenHeight = height / 2;
            DateItem item = new DateItem(startDate, width, childrenHeight);
            add(item);
            CalendarUtil.addDaysToDate(startDate, 1);
            item = new DateItem(startDate, width, childrenHeight);
            add(item);
        }
    }

    private class DateItem extends FlowPanel {
        private final Date date;

        private DateItem(final Date date, int width, int height) {
            this.date = CalendarUtil.copyDate(date);
            width -= 1;
            height -= 1;
            if (CalendarUtil.isSameDate(date, tableModel.getSelectedDate())) {
                setStyleName("calendar-focus-day-block");
                height -= 2;
                width -= 2;
            } else {
                setStyleName("calendar-day-block");
            }
            addStyle();
            getElement().getStyle().setWidth(width, Style.Unit.PX);
            getElement().getStyle().setHeight(height, Style.Unit.PX);

            final Label label = new Label(date.getDate() + " " + GuiUtil.MONTHS[date.getMonth()]);
            label.setStyleName("calendar-block-date");
            add(label);
            add(getTasksPanel());
        }

        private void addStyle() {
            final int dayIndex = date.getDay();
            if (dayIndex < 6) {
                addStyleName("calendar-work-day-block");
            } else {
                addStyleName("calendar-holiday-block");
            }
            if (CalendarUtil.isSameDate(date, currentDate)) {
                addStyleName("today");
            }
        }

        private Widget getTasksPanel() {
            final FlowPanel result = new FlowPanel();
            final CalendarTableModelCallback callback = new CalendarTableModelCallbackImpl(result);
            tableModel.fillByDateValues(date, callback);
            return result;
        }
    }

    private class CalendarTableModelCallbackImpl implements CalendarTableModelCallback {
        private final Panel container;

        private CalendarTableModelCallbackImpl(final Panel container) {
            this.container = container;
        }

        @Override
        public void fillValues(List<CalendarItemData> values) {
            if (values != null && !values.isEmpty()) {
                for (CalendarItemData calendarItemData : values) {
                    if (calendarItemData.getImage() != null) {
                        final HorizontalPanel wrapper = new HorizontalPanel();
                        final Image image = new Image(calendarItemData.getImage());
                        image.setWidth(calendarItemData.getImageWidth());
                        image.setHeight(calendarItemData.getImageHeight());
                        wrapper.add(image);
                        wrapper.add(getDescription(calendarItemData));
                        container.add(wrapper);
                    } else {
                        container.add(getDescription(calendarItemData));
                    }
                }
            }
        }

        private Label getDescription(CalendarItemData itemData) {
            final Label result = new Label(itemData.getDescription(), false);
            return result;
        }
    }
}
