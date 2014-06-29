package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DateBoxWidget;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;

import java.util.Date;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.06.2014
 *         Time: 22:53
 */
public class DateTimeBoxDecorate extends Composite {

    private DateBox dateBox;
    private OneDatePickerPopup picker;
    private FocusPanel dateBtn;
    private AbsolutePanel root;
    private ListBox timeZoneChooser;
    private DateBoxWidget parentWidget;
    private EventBus eventBus;
    public DateTimeBoxDecorate(DateBoxWidget parentWidget) {
        root = new AbsolutePanel();
        this.parentWidget = parentWidget;
        initWidget(root);
    }

    public void setValue(final DateBoxState state) {
        if (dateBox == null) {
            initRoot(state);
        } else {
            final Date date = getDate(state.getDateTimeContext());
            dateBox.setValue(date, false);
        }
    }

    public String getText() {
        final String result = dateBox.getValue() == null
                ? null
                : DateTimeFormat.getFormat(ModelUtil.DTO_PATTERN).format(dateBox.getValue());
        return result;
    }

    public String getSelectedTimeZoneId() {
        return timeZoneChooser == null ? null : timeZoneChooser.getItemText(timeZoneChooser.getSelectedIndex());
    }

    private void initRoot(final DateBoxState state) {

        root.setStyleName("wrap-date");
        eventBus = new SimpleEventBus();
        final ClickHandler showDatePickerHandler = new ShowDatePickerHandler();

        dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");
        final Date date = getDate(state.getDateTimeContext());
        final DateTimeFormat dtFormat = DateTimeFormat.getFormat(state.getPattern());

        DateBox.Format format = new DateBox.DefaultFormat(dtFormat);
        dateBox = new DateBox();
        dateBox.setFormat(format);
        dateBox.setValue(date);
        dateBox.getTextBox().addStyleName("date-text-box");
        picker = new OneDatePickerPopup(date, eventBus, state.isDisplayTime());
        // dateBox.getTextBox().addClickHandler(showDatePickerHandler);
        dateBtn.addClickHandler(showDatePickerHandler);
        Event.sinkEvents(dateBox.getElement(), Event.ONBLUR);
        dateBox.addHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                parentWidget.validate();
            }
        }, BlurEvent.getType());
        dateBox.getTextBox().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                dateBox.hideDatePicker();
            }
        });

        root.add(dateBox);
        root.add(dateBtn);
        if (state.isDisplayTimeZoneChoice()) {
            // show time zone chooser.
            timeZoneChooser = getTimeZoneBox(state.getDateTimeContext().getTimeZoneId());
            root.add(timeZoneChooser);
        }
        eventBus.addHandler(DateSelectedEvent.TYPE, new DateSelectedEventHandler() {
            @Override
            public void onDateSelected(DateSelectedEvent event) {
                dateBox.setValue(event.getDate());
            }
        });
    }

    private Date getDate(DateTimeContext context) {
        final Date result = context.getDateTime() == null
                ? null
                : DateTimeFormat.getFormat(ModelUtil.DTO_PATTERN).parse(context.getDateTime());
        return result;
    }

    private ListBox getTimeZoneBox(final String selectedId) {
        final ListBox result = new ListBox();
        final List<String> timeZoneIds = Application.getInstance().getTimeZoneIds();
        for (String timeZoneId : timeZoneIds) {
            result.addItem(timeZoneId);
        }
        final int index = timeZoneIds.indexOf(selectedId);
        result.setSelectedIndex(index < 0 ? 0 : index);

        return result;
    }

    public Date getValue() {
        return dateBox.getValue();
    }

    public DateBox getDateBox() {
        return dateBox;
    }

    private class ShowDatePickerHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            final String baloonUp;
            final String baloonDown;
            if (dateBtn.getAbsoluteTop() > dateBox.getDatePicker().getAbsoluteTop()) {
                baloonUp = "date-picker-baloon-up";
                baloonDown = "!!!";
            } else {
                baloonDown = "date-picker-baloon";
                baloonUp = "!!!";
            }
            picker.showRelativeTo(dateBox);
        }
    }
}
