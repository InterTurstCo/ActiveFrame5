package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.Date;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2014
 *         Time: 23:12
 */
public class OneDatePickerPopup extends DatePickerPopup {

    public OneDatePickerPopup(Date date, EventBus eventBus, boolean showTime) {
        super(eventBus);

        initWidget(date, showTime);
    }

    private void initWidget(Date date, boolean showTime) {

        Panel container = new AbsolutePanel();

        Panel nowPanel = initDateSelector(TODAY_LABEL, new EnumDateClickHandler(TODAY_LABEL));
        container.add(nowPanel);

        Panel tomorrowPanel = initDateSelector(TOMORROW_LABEL, new EnumDateClickHandler(TOMORROW_LABEL));
        container.add(tomorrowPanel);

        Panel yesterdayPanel = initDateSelector(YESTERDAY_LABEL,new EnumDateClickHandler(YESTERDAY_LABEL));
        container.add(yesterdayPanel);

        Panel nextWeekPanel = initDateSelector(NEXT_WEEK_LABEL, new EnumDateClickHandler(NEXT_WEEK_LABEL));
        container.add(nextWeekPanel);

        Panel lastWeekPanel = initDateSelector(LAST_WEEK_LABEL, new EnumDateClickHandler(LAST_WEEK_LABEL));
        container.add(lastWeekPanel);

        Panel nextYearPanel = initDateSelector(NEXT_YEAR_LABEL, new EnumDateClickHandler(NEXT_YEAR_LABEL));
        container.add(nextYearPanel);

        Panel lastYearPanel = initDateSelector(LAST_YEAR_LABEL, new EnumDateClickHandler(LAST_YEAR_LABEL));
        container.add(lastYearPanel);

        Panel datePanel = initDateSelectorWithPicker(CHOSE_DATE_LABEL);
        container.add(datePanel);
        DateTimePicker dateTimePicker = new DateTimePicker(date, showTime);
        Panel dateTimePickerPanel = initDatePickerPanel(dateTimePicker);
        datePanel.addDomHandler(new DatetimeClickHandler(dateTimePickerPanel), ClickEvent.getType());
        container.add(dateTimePickerPanel);
        this.add(container);
        this.setStyleName("composite-datetime-picker");

    }


    private Panel initDatePickerPanel(final DateTimePicker dateTimePicker) {
        final Panel container = new AbsolutePanel();

        container.setStyleName("composite-date-time-container-hidden");
        container.add(dateTimePicker);
        Button submit = new Button(BusinessUniverseConstants.DATETIME_PICKER_BUTTON);
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Date date = dateTimePicker.getFullDate();
                eventBus.fireEvent(new DateSelectedEvent(date));
                container.setStyleName("composite-date-time-container-hidden");
                OneDatePickerPopup.this.hide();
            }
        });
        this.addCloseHandler(new HideDateTimePickerCloseHandler(container));
        container.add(submit);
        return container;
    }


    private class EnumDateClickHandler implements ClickHandler {
        String dateDescription;

        private EnumDateClickHandler(String dateDescription) {
            this.dateDescription = dateDescription;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            Date date = getRequiredDate(dateDescription);
            eventBus.fireEvent(new DateSelectedEvent(date));
            OneDatePickerPopup.this.hide();
        }

        private Date getRequiredDate(String dateDescription) {
            Date date = new Date();
            switch (dateDescription) {
                case TODAY_LABEL:
                    break;
                case TOMORROW_LABEL:
                    CalendarUtil.addDaysToDate(date, 1);
                    break;
                case YESTERDAY_LABEL:
                    CalendarUtil.addDaysToDate(date, -1);
                    break;
                case LAST_WEEK_LABEL:
                    CalendarUtil.addDaysToDate(date, -7);
                    break;
                case NEXT_WEEK_LABEL:
                    CalendarUtil.addDaysToDate(date, 7);
                    break;
                case LAST_YEAR_LABEL:
                    CalendarUtil.addDaysToDate(date, -365);
                    break;
                case NEXT_YEAR_LABEL:
                    CalendarUtil.addDaysToDate(date, 365);
                    break;

            }
            return date;
        }

    }


    @Override
    public void show() {
        super.show();

    }
}
