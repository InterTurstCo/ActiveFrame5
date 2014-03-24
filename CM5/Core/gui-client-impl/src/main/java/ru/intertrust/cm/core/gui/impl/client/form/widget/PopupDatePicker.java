package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**
 * Created by User on 21.03.2014.
 */
public class PopupDatePicker extends DateBox {

    private PopupPanel popupPanel;

    public PopupDatePicker(DatePicker picker, Date date, Format format) {
        super(picker, date, format);

     init();
    }


    public void setPosition(int x, int y){
    popupPanel.setPopupPosition(x, y);
}

    private void init() {
        popupPanel = new PopupPanel();
        popupPanel.removeStyleName("gwt-PopupPanel");
        popupPanel.add(getDatePicker());
    }
    public void showDatePicker(){

        popupPanel.show();

    }


    @Override
    public Widget asWidget() {
        return popupPanel;
    }
}
