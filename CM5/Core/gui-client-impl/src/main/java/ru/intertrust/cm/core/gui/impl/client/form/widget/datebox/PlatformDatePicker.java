package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import com.google.gwt.user.datepicker.client.DefaultMonthSelector;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.03.2015
 *         Time: 17:56
 */
public class PlatformDatePicker extends DatePicker {
    public PlatformDatePicker(CalendarModel model) {
        super(new DefaultMonthSelector(), new DefaultCalendarView(), model);
    }
}
