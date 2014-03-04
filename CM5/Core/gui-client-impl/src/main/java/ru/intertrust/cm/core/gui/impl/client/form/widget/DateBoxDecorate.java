package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

import java.util.Date;

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

    public DateBoxDecorate() {
        root = new AbsolutePanel();
        root.setStyleName("wrap-date");
        initWidget(root);
    }

    public void setValue(final DateTimeContext context) {
        if (dateBox == null) {
            initRoot(context);
        } else {
            dateBox.setValue(dateBox.getFormat().parse(dateBox, context.getDateTime(), false));
        }
    }

    public String getText() {
        return dateBox.getFormat().format(dateBox, dateBox.getValue());
    }

    /**
     * todo investigate usages.
     * @return
     */
    public Date getValue() {
        return dateBox.getValue();
    }

    private void initRoot(DateTimeContext context) {
        final ClickHandler showDatePickerHandler = new ShowDatePickerHandler();
        picker = new CMJDatePicker();
        dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");
        final DateTimeFormat dtFormat = DateTimeFormat.getFormat(context.getPattern());
        final Date date = dtFormat.parse(context.getDateTime());
        DateBox.Format format = new DateBox.DefaultFormat(dtFormat);
        dateBox = new DateBox(picker, date, format);
        dateBox.getTextBox().addStyleName("date-text-box");
        dateBox.getTextBox().addClickHandler(showDatePickerHandler);
        dateBtn.addClickHandler(showDatePickerHandler);
        root.add(dateBox);
        root.add(dateBtn);
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
