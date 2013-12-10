package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TextBox;
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

    private DateBox dateBox;
    private CMJDatePicker picker;

    public DateBoxDecorate(){
        AbsolutePanel root = new AbsolutePanel();
        root.setStyleName("wrap-date");
        final FocusPanel dateBtn = new FocusPanel();
        dateBtn.setStyleName("date-box-button");

        DateBox.Format format = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
        picker = new CMJDatePicker();

        //dateBox = new DateBox(picker, null, format);
        dateBox = new DateBox(picker, null, format);
        TextBox textDateBox = dateBox.getTextBox();
        dateBox.getTextBox().addStyleName("date-text-box");
        root.add(dateBox);
        root.add(dateBtn);

        textDateBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String baloonUp;
                String baloonDown;
                dateBox.showDatePicker();

                if(dateBtn.getAbsoluteTop() > dateBox.getDatePicker().getAbsoluteTop()){
                    baloonUp =  "date-picker-baloon-up";
                    baloonDown = "!!!";
                }
                else{
                    baloonDown = "date-picker-baloon";
                    baloonUp =  "!!!";
                }

                picker.toggle(baloonDown, baloonUp);

            }
        });

        dateBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String baloonUp;
                String baloonDown;
                dateBox.showDatePicker();
                if(dateBtn.getAbsoluteTop() > dateBox.getDatePicker().getAbsoluteTop()){
                    baloonUp =  "date-picker-baloon-up";
                    baloonDown = "!!!";
                }
                else{
                    baloonDown = "date-picker-baloon";
                    baloonUp =  "!!!";
                }
                picker.toggle(baloonDown, baloonUp);
            }
        });

        initWidget(root);
       }

    public void setValue(Date date) {
        dateBox.setValue(date, false);
    }

    public Date getValue() {
        return dateBox.getValue();
    }
}
