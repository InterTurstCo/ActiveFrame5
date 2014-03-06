package ru.intertrust.cm.core.gui.impl.client.form.widget;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;

/**
 * @author Timofiy Bilyi
 *         Date: 02.12.13
 *         Time: 11:08
 */
public class DateBoxDecorate extends Composite {

    private DateBox dateBox;
    private CMJDatePicker picker;
    private FocusPanel dateBtn;
    private AbsolutePanel root;
    private ListBox timeZoneChooser;

    public DateBoxDecorate() {
        root = new AbsolutePanel();
        root.setStyleName("wrap-date");
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
                : DateTimeFormat.getFormat(DateTimeContext.DTO_PATTERN).format(dateBox.getValue());
        return result;
    }

    public String getSelectedTimeZoneId() {
        return timeZoneChooser == null ? null : timeZoneChooser.getItemText(timeZoneChooser.getSelectedIndex());
    }

    /**
     * todo investigate usages.
     * @return
     */
    public Date getValue() {
        return dateBox.getValue();
    }

    private void initRoot(final DateBoxState state) {
        final ClickHandler showDatePickerHandler = new ShowDatePickerHandler();
        picker = new CMJDatePicker();
        dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");
        final Date date = getDate(state.getDateTimeContext());
        final DateTimeFormat dtFormat = DateTimeFormat.getFormat(state.getPattern());
        DateBox.Format format = new DateBox.DefaultFormat(dtFormat);
        dateBox = new DateBox(picker, date, format);
        dateBox.getTextBox().addStyleName("date-text-box");
        dateBox.getTextBox().addClickHandler(showDatePickerHandler);
        dateBtn.addClickHandler(showDatePickerHandler);
        root.add(dateBox);
        root.add(dateBtn);
        if (state.isDisplayTimeZoneChoice()
                && state.getDateTimeContext().getOrdinalFieldType() == FieldType.DATETIMEWITHTIMEZONE.ordinal()) {
            // show time zone chooser.
            timeZoneChooser = getTimeZoneBox(state.getDateTimeContext().getTimeZoneId());
            root.add(timeZoneChooser);
        }
    }

    private Date getDate(DateTimeContext context) {
        final Date result = context.getDateTime() == null
                ? null
                : DateTimeFormat.getFormat(DateTimeContext.DTO_PATTERN).parse(context.getDateTime());
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

    private class ShowDatePickerHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            final String baloonUp;
            final String baloonDown;
            if(dateBtn.getAbsoluteTop() > dateBox.getDatePicker().getAbsoluteTop()){
                baloonUp =  "date-picker-baloon-up";
                baloonDown = "!!!";
            } else{
                baloonDown = "date-picker-baloon";
                baloonUp =  "!!!";
            }
            picker.toggle(baloonDown, baloonUp);
            dateBox.showDatePicker();
        }
    }
}
