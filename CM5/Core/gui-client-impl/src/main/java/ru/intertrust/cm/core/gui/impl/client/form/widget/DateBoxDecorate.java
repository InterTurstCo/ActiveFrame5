package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 02.12.13
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
public class DateBoxDecorate extends Composite {
    //DateBox dateBox;
    DateBox dateBox;


    public DateBoxDecorate(){
        AbsolutePanel root = new AbsolutePanel();
        root.setStyleName("wrap-date");
        FocusPanel dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");

        DateBox.Format format = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
        CMJDatePicker picker = new CMJDatePicker();

        //dateBox = new DateBox(picker, null, format);
        dateBox = new DateBox(picker, null, format);
        dateBox.getTextBox().addStyleName("date-text-box");
        root.add(dateBox);
        root.add(dateBtn);
        initWidget(root);
       }

    public void setValue(Date date) {
        dateBox.setValue(date, false);


        //text.setText(DateBoxWidget.DATE_TIME_FORMAT.format(date));
    }

    public Date getValue() {
        return dateBox.getValue();
    }


//    public TextBox getTextField() {
//    //    return text;
//    }
}
