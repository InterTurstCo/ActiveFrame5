package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.RangeDateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.07.2014
 *         Time: 21:41
 */
public class CollectionRangeDatePicker extends RangeDatePicker {
    public CollectionRangeDatePicker(Date startDate, Date endDate, EventBus eventBus, boolean showTime, boolean showSeconds) {
        super(startDate, endDate, eventBus, showTime, showSeconds);
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
                eventBus.fireEventFromSource(new RangeDateSelectedEvent(startDate, endDate), CollectionRangeDatePicker.this);
                eventBus.fireEvent(new FilterEvent(false));
                container.setStyleName("compositeDateTimeContainerHidden");
                CollectionRangeDatePicker.this.hide();
            }
        });
        this.addCloseHandler(new HideDateTimePickerCloseHandler(container));
        submit.setStyleName("darkButton");
        container.add(submit);
        return container;
    }

    protected void initWidget(Date startDate, Date endDate, boolean showTime, boolean showSeconds) {

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
        final DateTimePicker startDateTimePicker = new DateTimePicker(startDate, showTime, showSeconds);
        final DateTimePicker endDateTimePicker = new DateTimePicker(endDate, showTime, showSeconds);
        Panel dateTimePickersPanel = initDatePickerPanel(startDateTimePicker, endDateTimePicker);
        dateRangePanel.addDomHandler(new DatetimeClickHandler(dateTimePickersPanel), ClickEvent.getType());
        container.add(dateTimePickersPanel);

        this.add(container);
        this.setStyleName("compositeDatetimePicker");

    }

    private class RangeEnumDateClickHandler implements ClickHandler {
        String dateDescription;

        private RangeEnumDateClickHandler(String dateDescription) {
            this.dateDescription = dateDescription;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            List<Date> dates = getRequiredDate(dateDescription);
            eventBus.fireEventFromSource(new RangeDateSelectedEvent(dates.get(0), dates.get(1)),
                    CollectionRangeDatePicker.this);
            eventBus.fireEvent(new FilterEvent(false));
            CollectionRangeDatePicker.this.hide();
        }

    }
}
