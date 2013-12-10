package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 03.12.13
 * Time: 18:02
 * To change this template use File | Settings | File Templates.
 */
public class CMJDatePicker extends DatePicker {
      private AbsolutePanel baloon;
      private AbsolutePanel bottomBaloon;
      private AbsolutePanel decorateDatePicker;

    @Override
    protected void setup() {
        decorateDatePicker = new AbsolutePanel();
        decorateDatePicker.setStyleName("date-picker-decorate");
        baloon = new AbsolutePanel();
        baloon.setStyleName("date-picker-baloon");
        VerticalPanel panel = new VerticalPanel();
        decorateDatePicker.add(panel);
        decorateDatePicker.add(baloon);
        decorateDatePicker.add(panel);
        bottomBaloon = new AbsolutePanel();
        decorateDatePicker.add(bottomBaloon);
        initWidget(decorateDatePicker);
        panel.add(this.getMonthSelector());
        panel.add(this.getView());
    }

    public void toggle(String styleUp, String styleDown ) {
        baloon.setStyleName("!!!");
        baloon.removeStyleName(styleUp);
        bottomBaloon.removeStyleName(styleDown);
        baloon.setStyleName(styleUp);
        bottomBaloon.setStyleName(styleDown);
    }

    public AbsolutePanel getBaloon() {
        return baloon;
    }
}
