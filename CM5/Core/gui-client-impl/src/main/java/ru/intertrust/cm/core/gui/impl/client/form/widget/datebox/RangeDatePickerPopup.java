package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 0:48
 */
public class RangeDatePickerPopup extends DatePickerPopup {
    public RangeDatePickerPopup(Date startDate, Date endDate, EventBus eventBus, boolean showTime) {
        super(eventBus);
        initWidget(startDate, endDate, showTime);
    }

    private void initWidget(Date startDate, Date endDate, boolean showTime) {

        Panel container = new AbsolutePanel();

        Panel forTodayPanel = initDateSelector(FOR_TODAY_LABEL, new RangeEnumDateClickHandler(FOR_TODAY_LABEL));
        container.add(forTodayPanel);

        Panel forYesterdayPanel = initDateSelector(FOR_YESTERDAY_LABEL, new RangeEnumDateClickHandler(FOR_YESTERDAY_LABEL));
        container.add(forYesterdayPanel);

        Panel forLastWeekPanel = initDateSelector(FOR_LAST_WEEK_LABEL, new RangeEnumDateClickHandler(FOR_LAST_WEEK_LABEL));
        container.add(forLastWeekPanel);

        Panel forLastYearPanel = initDateSelector(FOR_LAST_YEAR_LABEL, new RangeEnumDateClickHandler(FOR_LAST_YEAR_LABEL));
        container.add(forLastYearPanel);

        Panel dateRangePanel = initDateSelectorWithPicker(CHOSE_DATE_RANGE_LABEL);
        container.add(dateRangePanel);
        final DateTimePicker startDateTimePicker = new DateTimePicker(startDate, showTime);
        final DateTimePicker endDateTimePicker = new DateTimePicker(endDate, showTime);
        Panel dateTimePickersPanel = initDatePickerPanel(startDateTimePicker, endDateTimePicker);
        dateRangePanel.addDomHandler(new DatetimeClickHandler(dateTimePickersPanel), ClickEvent.getType());
        container.add(dateTimePickersPanel);

        this.add(container);
        this.setStyleName("composite-datetime-picker");

    }

    private Panel initDatePickerPanel(final DateTimePicker startDateTimePicker, final DateTimePicker endDateTimePicker) {
        final Panel container = new AbsolutePanel();

        container.setStyleName("composite-date-time-container-hidden");
        container.add(startDateTimePicker);
        container.add(endDateTimePicker);
        Button submit = new Button(BusinessUniverseConstants.DATETIME_PICKER_BUTTON);
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Date startDate = startDateTimePicker.getFullDate();
                Date endDate = endDateTimePicker.getFullDate();
                eventBus.fireEvent(new RangeDateSelectedEvent(startDate, endDate));
                container.setStyleName("composite-date-time-container-hidden");
                RangeDatePickerPopup.this.hide();
            }
        });
        this.addCloseHandler(new HideDateTimePickerCloseHandler(container));
        submit.setStyleName("composite-datetime-submit-button dark-button");
        container.add(submit);
        return container;
    }

    private class RangeEnumDateClickHandler implements ClickHandler {
        String dateDescription;

        private RangeEnumDateClickHandler(String dateDescription) {
            this.dateDescription = dateDescription;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            List<Date> dates = getRequiredDate(dateDescription);
            eventBus.fireEvent(new RangeDateSelectedEvent(dates.get(0), dates.get(1)));
            RangeDatePickerPopup.this.hide();
        }

        private List<Date> getRequiredDate(String dateDescription) {
            Date startDate = new Date();
            Date endDate = new Date();

            switch (dateDescription) {
                case FOR_TODAY_LABEL:
                    CalendarUtil.addDaysToDate(startDate, -1);
                    return getRangeDateList(startDate, endDate);

                case FOR_YESTERDAY_LABEL:
                    CalendarUtil.addDaysToDate(startDate, -2);
                    CalendarUtil.addDaysToDate(endDate, -1);
                    break;
                case FOR_LAST_WEEK_LABEL:
                    CalendarUtil.addDaysToDate(startDate, -7);
                    getRangeDateList(startDate, endDate);
                    break;
                case FOR_LAST_YEAR_LABEL:
                    CalendarUtil.addDaysToDate(startDate, -365);
                    getRangeDateList(startDate, endDate);
                    break;


            }
            return null;
        }

        private List<Date> getRangeDateList(Date startDate, Date endDate) {
            List<Date> dates = new ArrayList<Date>();
            dates.add(startDate);
            dates.add(endDate);
            return dates;
        }

    }
}
