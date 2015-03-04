package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.09.2014
 *         Time: 18:49
 */
public class CollectionDatePicker extends OneDatePicker {
    public CollectionDatePicker(Date date, EventBus eventBus, boolean showTime, boolean showSeconds) {
        super(date, eventBus, showTime, showSeconds);
    }

    @Override
    protected Panel initDatePickerPanel(final DateTimePicker dateTimePicker) {
        final Panel container = new AbsolutePanel();
        container.add(dateTimePicker);
        Button submit = new Button(LocalizeUtil.get(BusinessUniverseConstants.DATETIME_PICKER_BUTTON));
        submit.setStyleName("darkButton");
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Date date = dateTimePicker.getFullDate();
                eventBus.fireEventFromSource(new DateSelectedEvent(date), CollectionDatePicker.this);
                eventBus.fireEvent(new FilterEvent(false));
                CollectionDatePicker.this.hide();
            }
        });
        this.addCloseHandler(new HideDateTimePickerCloseHandler(container));
        container.add(submit);
        return container;
    }
}
