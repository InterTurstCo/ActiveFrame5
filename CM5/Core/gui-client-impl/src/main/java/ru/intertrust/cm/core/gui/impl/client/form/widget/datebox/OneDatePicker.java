package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

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
public class OneDatePicker extends DatePickerPopup {

    public OneDatePicker(Date date, EventBus eventBus, boolean showTime, boolean showSeconds) {
        super(eventBus);

        initWidget(date, showTime, showSeconds);
    }

    private void initWidget(Date date, boolean showTime, boolean showSeconds) {

        DateTimePicker dateTimePicker = new DateTimePicker(date, showTime, showSeconds);
        Panel dateTimePickerPanel = initDatePickerPanel(dateTimePicker);
        Panel container = new AbsolutePanel();
        container.add(dateTimePickerPanel);
        this.add(container);
        this.setStyleName("compositeDatetimePicker");

    }

    //TODO make abstract
    protected Panel initDatePickerPanel(final DateTimePicker dateTimePicker) {
        final Panel container = new AbsolutePanel();

        container.add(dateTimePicker);
        Button submit = new Button(BusinessUniverseConstants.DATETIME_PICKER_BUTTON);
        submit.setStyleName("dark-button");
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Date date = dateTimePicker.getFullDate();
                eventBus.fireEvent(new DateSelectedEvent(date));
                OneDatePicker.this.hide();
            }
        });
        this.addCloseHandler(new HideDateTimePickerCloseHandler(container));
        container.add(submit);
        return container;
    }

}
