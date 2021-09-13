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
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeEndConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeStartConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.datechange.DateSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DateBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.localization.PlatformDateTimeFormat;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.06.2014
 *         Time: 22:53
 */
public class DecoratedDateTimeBox extends Composite {

    private DecoratedDateBox    dateBox;
    private DatePickerPopup     picker;
    private AbsolutePanel       root;
    private ListBox             timeZoneChooser;
    private DateBoxWidget       parentWidget;
    private boolean             isRangeStart;
    private boolean             isRangeEnd;
    private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public DecoratedDateTimeBox(DateBoxWidget parentWidget) {
        root = new AbsolutePanel();
        this.parentWidget = parentWidget;
        initWidget(root);
    }

    @Override
    protected void onDetach() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
        root.clear();
        dateBox = null;
        picker = null;
    }

    public void setValue(final DateBoxState state) {
        if (dateBox == null) {
            initRoot(state);
        } else {
            final Date date = getDate(state.getDateTimeContext());
            dateBox.setValue(date, false);
        }
    }

    public void setValue(Date date) {
        dateBox.setValue(date);
    }

    public String getText() {
        return dateBox.getValue() == null ? null
                : PlatformDateTimeFormat.getFormat(ModelUtil.DTO_PATTERN).format(dateBox.getValue());
    }

    public String getSelectedTimeZoneId() {
        return timeZoneChooser == null ? null : timeZoneChooser.getItemText(timeZoneChooser.getSelectedIndex());
    }

    private void initRoot(final DateBoxState state) {
        root.setStyleName("wrap-date");
        root.getElement().getStyle().clearPosition();
        RangeStartConfig rangeStartConfig = state.getDateBoxConfig().getRangeStartConfig();
        RangeEndConfig rangeEndConfig = state.getDateBoxConfig().getRangeEndConfig();
        isRangeStart = rangeEndConfig != null;
        isRangeEnd = rangeStartConfig != null;
        boolean isSearchRangePopup = isRangeStart || isRangeEnd;
        EventBus eventBus = isSearchRangePopup ? parentWidget.getEventBus() : new SimpleEventBus();
        final ClickHandler showDatePickerHandler = new ShowDatePickerHandler();

        FocusPanel dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");
        final Date date = getDate(state.getDateTimeContext());
        String pattern = state.getPattern();
        final DateTimeFormat dtFormat = PlatformDateTimeFormat.getFormat(pattern);

        DateBox.Format format = new DateBox.DefaultFormat(dtFormat);
        dateBox = new DecoratedDateBox();
        dateBox.setFormat(format);
        dateBox.setValue(date);
        dateBox.getTextBox().addStyleName("date-text-box");
        boolean showSeconds = TimeUtil.showSeconds(pattern);

        picker = isSearchRangePopup
                ? new FormRangeDatePicker(date, null, eventBus, state.isDisplayTime(),
                showSeconds, rangeStartConfig, rangeEndConfig)
                : new FormDatePicker(date, eventBus, state.isDisplayTime(), showSeconds);

        dateBtn.addClickHandler(showDatePickerHandler);
        Event.sinkEvents(dateBox.getElement(), Event.ONBLUR);
        dateBox.addHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                parentWidget.validate();
            }
        }, BlurEvent.getType());

        root.add(dateBox);
        root.add(dateBtn);
        if (state.isDisplayTimeZoneChoice()) {
            // show time zone chooser.
            timeZoneChooser = getTimeZoneBox(state.getDateTimeContext().getTimeZoneId());
            root.add(timeZoneChooser);
        }
        handlerRegistrations.add(eventBus.addHandler(DateSelectedEvent.TYPE, new DateSelectedEventHandler() {
            @Override
            public void onDateSelected(DateSelectedEvent event) {
                if(picker.equals(event.getSource())){
                    parentWidget.clearErrors();
                    dateBox.setValue(event.getDate());
                }
            }
        }));
    }

    private Date getDate(DateTimeContext context) {
        final Date result = context.getDateTime() == null
                ? null
                : PlatformDateTimeFormat.getFormat(ModelUtil.DTO_PATTERN).parse(context.getDateTime());
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

    public Object getValue() {
        return dateBox.getTextBox().getValue();
    }

    public DateBox getDateBox() {
        return dateBox;
    }

    private class ShowDatePickerHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            picker.showRelativeTo(dateBox);
            if (isRangeStart) {
                ((FormRangeDatePicker)picker).setStartDate(dateBox.getValue());
            } else if (isRangeEnd) {
                ((FormRangeDatePicker)picker).setEndDate(dateBox.getValue());
            } else {
                ((FormDatePicker)picker).setDate(dateBox.getValue());
            }
        }
    }
}
