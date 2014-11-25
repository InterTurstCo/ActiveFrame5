package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeEndConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeStartConfig;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.FormRangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.07.2014
 *         Time: 0:38
 */
public class FormRangeDatePicker extends RangeDatePicker {
    private RangeStartConfig rangeStartConfig;
    private RangeEndConfig rangeEndConfig;
    private DateTimePicker startDateTimePicker;
    private DateTimePicker endDateTimePicker;

    public FormRangeDatePicker(Date startDate, Date endDate, EventBus eventBus, boolean showTime,
                               boolean showSeconds) {
        super(startDate, endDate, eventBus, showTime, showSeconds);
    }

    @Override
    protected void initWidget(Date startDate, Date endDate, boolean showTime, boolean showSeconds) {
        Panel container = new AbsolutePanel();

        Panel forTodayPanel = initDateSelector(FOR_TODAY_LABEL, new FormRangeEnumDateClickHandler(FOR_TODAY_LABEL));
        container.add(forTodayPanel);

        Panel forYesterdayPanel = initDateSelector(FOR_YESTERDAY_LABEL, new FormRangeEnumDateClickHandler(FOR_YESTERDAY_LABEL));
        container.add(forYesterdayPanel);

        Panel forLastWeekPanel = initDateSelector(FOR_LAST_WEEK_LABEL, new FormRangeEnumDateClickHandler(FOR_LAST_WEEK_LABEL));
        container.add(forLastWeekPanel);

        Panel forLastYearPanel = initDateSelector(FOR_LAST_YEAR_LABEL, new FormRangeEnumDateClickHandler(FOR_LAST_YEAR_LABEL));
        container.add(forLastYearPanel);

        Panel dateRangePanel = initDateSelectorWithPicker(CHOSE_DATE_RANGE_LABEL);
        container.add(dateRangePanel);
        startDateTimePicker = new DateTimePicker(startDate, showTime, showSeconds);
        endDateTimePicker = new DateTimePicker(endDate, showTime, showSeconds);
        Panel dateTimePickersPanel = initDatePickerPanel(startDateTimePicker, endDateTimePicker);
        dateRangePanel.addDomHandler(new DatetimeClickHandler(dateTimePickersPanel), ClickEvent.getType());
        container.add(dateTimePickersPanel);

        this.add(container);
        this.setStyleName("compositeDatetimePicker");
    }

    public FormRangeDatePicker(Date startDate, Date endDate, EventBus eventBus, boolean showTime,
                               boolean showSeconds, RangeStartConfig rangeStartConfig, RangeEndConfig rangeEndConfig) {
        super(startDate, endDate, eventBus, showTime, showSeconds);
        this.rangeStartConfig = rangeStartConfig;
        this.rangeEndConfig = rangeEndConfig;
    }

    protected Panel initDatePickerPanel(final DateTimePicker startDateTimePicker, final DateTimePicker endDateTimePicker) {
        final Panel container = new AbsolutePanel();

        container.setStyleName("compositeDateTimeContainerHidden");
        container.add(startDateTimePicker);
        container.add(endDateTimePicker);
        Button submit = new Button(BusinessUniverseConstants.DATETIME_PICKER_BUTTON);
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Date startDate = startDateTimePicker.getFullDate();
                Date endDate = endDateTimePicker.getFullDate();
                eventBus.fireEvent(new FormRangeDateSelectedEvent(startDate, endDate, rangeStartConfig, rangeEndConfig));
                container.setStyleName("compositeDateTimeContainerHidden");
                FormRangeDatePicker.this.hide();
            }
        });
        submit.setStyleName("darkButton");
        container.add(submit);
        return container;
    }

    public void setStartDate(Date date) {
        startDateTimePicker.setDate(date);
    }

    public void setEndDate(Date date) {
        endDateTimePicker.setDate(date);
    }

    private class FormRangeEnumDateClickHandler implements ClickHandler {
        private String dateDescription;

        private FormRangeEnumDateClickHandler(String dateDescription) {
            this.dateDescription = dateDescription;

        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            List<Date> dates = getRequiredDate(dateDescription);
            eventBus.fireEvent(new FormRangeDateSelectedEvent(dates.get(0), dates.get(1), rangeStartConfig, rangeEndConfig));
            FormRangeDatePicker.this.hide();
        }
    }
}
