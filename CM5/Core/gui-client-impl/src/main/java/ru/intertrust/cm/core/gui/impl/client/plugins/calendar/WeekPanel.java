package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
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
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.WEEK_SWITCH_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.MONTHS;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.WEEK_DAYS;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.WEEK_SWITCH_BUTTON;
/**
 * @author Sergey.Okolot
 *         Created on 17.10.2014 14:17.
 */
public class WeekPanel extends AbstractCalendarPanel implements CalendarNextWeekEventHandler,
        CalendarPreviousWeekEventHandler, CalendarTodayEventHandler {

    private Date beginWeekDate;
    private FlowPanel switchBtn;

    public WeekPanel(final EventBus localEventBus, final CalendarTableModel tableModel, final CalendarConfig config) {
        super(localEventBus, tableModel, config);
        handlers.add(this.localEventBus.addHandler(CalendarTodayEvent.TYPE, this));
        handlers.add(this.localEventBus.addHandler(CalendarPreviousWeekEvent.TYPE, this));
        handlers.add(this.localEventBus.addHandler(CalendarNextWeekEvent.TYPE, this));
        switchBtn = createSwitchBtn();
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
        beginWeekDate = CalendarUtil.copyDate(currentDate);
        final int dayToMonday = beginWeekDate.getDay() - 1;
        CalendarUtil.addDaysToDate(beginWeekDate, -dayToMonday);
        tableModel.setSelectedDate(currentDate);
        calendarConfig.addHistoryValue(UserSettingsHelper.CALENDAR_SELECTED_DATE, currentDate);
        buildPresentation();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        beginWeekDate = CalendarUtil.copyDate(tableModel.getSelectedDate());
        final int dayToMonday = beginWeekDate.getDay() - 1;
        CalendarUtil.addDaysToDate(beginWeekDate, -dayToMonday);
        buildPresentation();
    }

    private void buildPresentation() {
        clear();
        final Date cursorDate = CalendarUtil.copyDate(beginWeekDate);
        final int dayCount = calendarConfig.isShowWeekend() ? 6 : 5;
        final int height = getOffsetHeight();
        final int width = getOffsetWidth() / dayCount;
        for (int index = 0; index < dayCount; index++) {
            if (index < 5) {
                final DateItem item = new DateItem(cursorDate, width, height);
                if (index == 4) {
                    setSwitchBtnStyle();
                    item.add(switchBtn);
                }
                add(item);
            } else {
                add(new WeekendItem(cursorDate, width, height));
            }
            CalendarUtil.addDaysToDate(cursorDate, 1);
        }
    }

    private void setSwitchBtnStyle() {
        if (calendarConfig.isShowWeekend()) {
            switchBtn.getElement().setClassName("weekPanelOnSwitchButton");
        } else {
            switchBtn.getElement().setClassName("weekPanelOffSwitchButton");
        }
    }

    private FlowPanel createSwitchBtn() {
        final FlowPanel result = new FlowPanel();
        result.add(new InlineLabel(LocalizeUtil.get(WEEK_SWITCH_BUTTON_KEY,
                WEEK_SWITCH_BUTTON)));
        result.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                calendarConfig.setShowWeekend(!calendarConfig.isShowWeekend());
                buildPresentation();
            }
        }, ClickEvent.getType());
        result.sinkEvents(Event.ONCLICK);
        return result;
    }

    private class WeekendItem extends AbstractWeekendItem {

        private WeekendItem(final Date startDate, int width, final int height) {
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
        protected Label getItemLabel(Date date) {
            final HTML result = new HTML(date.getDate() + " " + LocalizeUtil.get(MONTHS[date.getMonth()]) + ",<br/>"
                    + LocalizeUtil.get(WEEK_DAYS[date.getDay()]), true);
            result.setStyleName("calendarBlockDate");
            return result;
        }

        @Override
        protected void addStyles() {
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

        @Override
        protected Widget getTasksPanel() {
            final FlowPanel result = new FlowPanel();
            result.setStyleName("calendarItemsWrapper");
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
        public void fillValues(List<CalendarItemsData> values) {
            if (values != null && !values.isEmpty()) {
                for (CalendarItemsData calendarItemsData : values) {
                    if (calendarItemsData.getImage() != null) {
                        final Panel wrapper = new AbsolutePanel();
                        wrapper.setStyleName("calendarTaskWrapper");
                        final Image image = new Image(calendarItemsData.getImage());
                        image.setWidth(calendarItemsData.getImageWidth());
                        image.setHeight(calendarItemsData.getImageHeight());
                        wrapper.add(image);
                        addPresentations(calendarItemsData, wrapper);
                        container.add(wrapper);
                    } else {
                       addPresentations(calendarItemsData, container);
                    }
                }
            }
        }

        private void addPresentations(CalendarItemsData itemsData, Panel panel) {
            if (itemsData.getDayItems() == null) {
                panel.add(GuiUtil.getCalendarItemPresentation(itemsData.getMonthItem(), itemsData.getRootObjectId()));
            } else {
                for (CalendarItemData itemData: itemsData.getDayItems()) {
                    panel.add(GuiUtil.getCalendarItemPresentation(itemData, itemsData.getRootObjectId()));
                }
            }
        }

    }
}
