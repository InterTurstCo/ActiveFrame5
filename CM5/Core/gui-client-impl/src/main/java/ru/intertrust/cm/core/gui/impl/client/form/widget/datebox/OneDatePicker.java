package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2014
 *         Time: 23:12
 */
@Deprecated
/*
 * Don't instaniate directly,
 * use subclasses FormDatePicker and CollectionDatePicker instead
 */
public abstract class OneDatePicker extends DatePickerPopup {

    private DateTimePicker dateTimePicker;

    public OneDatePicker(Date date, EventBus eventBus, boolean showTime, boolean showSeconds) {
        super(eventBus);

        initWidget(date, showTime, showSeconds);
    }

    private void initWidget(Date date, boolean showTime, boolean showSeconds) {
        dateTimePicker = new DateTimePicker(date, showTime, showSeconds);
        Panel dateTimePickerPanel = initDatePickerPanel(dateTimePicker);
        Panel container = new AbsolutePanel();
        container.add(dateTimePickerPanel);
        this.add(container);
        this.setStyleName("compositeDatetimePicker");

    }

    protected abstract Panel initDatePickerPanel(final DateTimePicker dateTimePicker);

    public void setDate(Date date) {
        dateTimePicker.setDate(date);
    }
}
