package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 25/03/14
 *         Time: 12:05 PM
 */
public class PopupDatePicker extends PopupPanel {

    private PopupPanel popupPanel;
    private CMJDatePicker datePicker;

    public PopupDatePicker(CMJDatePicker picker) {
      this.datePicker = picker;
      init();
    }

    private void init() {
        popupPanel = new PopupPanel();
        popupPanel.removeStyleName("gwt-PopupPanel");
        popupPanel.getElement().getStyle().setZIndex(12);
        popupPanel.add(datePicker);
        popupPanel.addDomHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                popupPanel.hide();
            }
        }, MouseOutEvent.getType());
    }
    public void showDatePicker(){

        popupPanel.show();

    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    @Override
    public Widget asWidget() {
        return popupPanel;
    }
}
