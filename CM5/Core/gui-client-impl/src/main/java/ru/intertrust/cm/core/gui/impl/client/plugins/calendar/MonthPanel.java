package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import java.util.List;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModelCallback;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;

/**
 * @author Sergey.Okolot
 *         Created on 17.10.2014 17:58.
 */
public class MonthPanel extends CalendarPanel implements CalendarScrollEventHandler, MouseWheelHandler {
    private static int DATE_ITEM_HEIGHT = 98;

    private int containerOffset;

    public MonthPanel(final EventBus localEventBus, final CalendarTableModel tableModel, final CalendarConfig config) {
        super(localEventBus, tableModel, config);
        handlers.add(addHandler(this, MouseWheelEvent.getType()));
        sinkEvents(Event.ONMOUSEWHEEL);
    }

    @Override
    public void scrollTo(final Widget source, final Date date) {
        if (this != source) {
//            System.out.println("-----------------> MonthPanel " + date);
        }
    }

    @Override
    public void onMouseWheel(MouseWheelEvent event) {
        final int delta = event.getDeltaY();
        containerOffset -= delta;
        if (containerOffset < 0) {
            final WeekItem weekItem = (WeekItem) getWidget(0);
            final DateItem dateItem = (DateItem) weekItem.getWidget(0);
            final Date startDate = CalendarUtil.copyDate(dateItem.date);
            CalendarUtil.addDaysToDate(startDate, -7);
            remove(getWidgetCount() - 1);
            insert(new WeekItem(startDate, getOffsetWidth(), calendarConfig.isShowWeekend()), 0);
            containerOffset += DATE_ITEM_HEIGHT;
        } else if (containerOffset > DATE_ITEM_HEIGHT) {
            final WeekItem weekItem = (WeekItem) getWidget(getWidgetCount() - 1);
            final DateItem dateItem = (DateItem) weekItem.getWidget(0);
            final Date startDate = CalendarUtil.copyDate(dateItem.date);
            CalendarUtil.addDaysToDate(startDate, 7);
            remove(0);
            add(new WeekItem(startDate, getOffsetWidth(), calendarConfig.isShowWeekend()));
            containerOffset -= DATE_ITEM_HEIGHT;
        }
        getElement().getStyle().setMarginTop(-containerOffset, Style.Unit.PX);
        final WeekItem weekItem = (WeekItem) getWidget(0);
        final DateItem dateItem = (DateItem) weekItem.getWidget(0);
        final Date date = CalendarUtil.copyDate(dateItem.date);
        final int dateOffset =  containerOffset / (DATE_ITEM_HEIGHT / 7);
        CalendarUtil.addDaysToDate(date, 7 + dateOffset);
        localEventBus.fireEvent(new CalendarScrollEvent(this, date));
    }

    @Override
    public void onResize() {

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        final Date selectedDate = tableModel.getSelectedDate();
        final Date startDate = CalendarUtil.copyDate(selectedDate);
        CalendarUtil.addDaysToDate(startDate, - selectedDate.getDay() - 13);
        localEventBus.addHandler(CalendarScrollEvent.TYPE, this);
        initialize(startDate);
    }

    private void initialize(final Date startDate) {
        final int height = getOffsetHeight();
        int weekCount = height / DATE_ITEM_HEIGHT + 2;
        containerOffset = DATE_ITEM_HEIGHT;
        getElement().getStyle().setMarginTop( - containerOffset, Style.Unit.PX);
        final int offsetWidth = getOffsetWidth();
        for (int index = 0; index < weekCount; index++) {
            add(new WeekItem(startDate, offsetWidth, calendarConfig.isShowWeekend()));
            CalendarUtil.addDaysToDate(startDate, 7);
        }
    }

    private class WeekItem extends FlowPanel {

        private WeekItem(final Date startDate, final int width, final boolean showWeekend) {
            final Date date = CalendarUtil.copyDate(startDate);
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
            final Label result = new Label(date.getDate() + "");
            result.setStyleName("calendar-block-date");
            return result;
        }


        @Override
        protected Widget getTasksPanel() {
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
            if (values != null) {
                System.out.println("--------------> container: " + container);
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
