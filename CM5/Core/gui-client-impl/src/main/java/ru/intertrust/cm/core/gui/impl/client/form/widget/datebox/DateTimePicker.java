package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;


import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.datepicker.client.DatePicker;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.06.2014
 *         Time: 22:53
 */
public class DateTimePicker extends Composite {
    private DatePicker picker;
    private TimeBox timePicker;
    private Panel container;
    private Date date;
    private DatePickerPopup parent;

    public DateTimePicker(Date date, boolean showTime, boolean showSeconds) {
        this(date, showTime, showSeconds, null);
    }

    public DateTimePicker(Date date, boolean showTime, boolean showSeconds, DatePickerPopup parent) {
        this.date = date == null ? new Date() : date;
        this.parent = parent;
        initWidgetContent(showTime, showSeconds);
        initWidget(container);
    }

    private void initWidgetContent(boolean showTime, boolean showSeconds) {
        container = new AbsolutePanel();
        picker = new DatePicker();
        picker.setValue(date);
        picker.setStyleName("datePickerDecorate");
        container.add(picker);
        if (showTime) {
            timePicker = new TimeBox(date, showSeconds);
            container.add(timePicker);
        } else if (parent != null) {
            picker.addValueChangeHandler(new ValueChangeHandler<Date>() {
                @Override
                public void onValueChange(ValueChangeEvent<Date> event) {
                    Date date = getFullDate();
                    parent.eventBus.fireEventFromSource(new DateSelectedEvent(date), parent);
                    parent.hide();
                }
            });
        }

    }

    public Date getFullDate() {
        Date pickerDate = picker.getValue();
        Date realDate = (date != null && pickerDate == null) ? date : pickerDate;
        if (timePicker != null) {

            int hours = timePicker.getHours();
            int minutes = timePicker.getMinutes();
            int seconds = timePicker.getSeconds();
            realDate.setHours(hours);
            realDate.setMinutes(minutes);
            realDate.setSeconds(seconds);
        }
        return realDate;
    }

    public void setDate(Date date) {
        this.date = date;
        picker.setValue(date);
    }

    public boolean showTime() {
        return timePicker != null;
    }
}
