package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by lvov on 04.04.14.
 */
public class CalendarHolidayView extends CalendarDayView {


    private FocusPanel saturday = new FocusPanel();
    private FocusPanel sunday = new FocusPanel();
    private Label date = new Label();
    private Label date2 = new Label();
    private Date saturdayDate;
    private Date sundayDate;


    public CalendarHolidayView(Date saturdayDate, Date sundayDate, EventBus eventBus, Plugin plugin) {
        super(eventBus, plugin);
        this.saturdayDate = saturdayDate;
        this.sundayDate = sundayDate;


        buildWeekendBlock();
        setStyle();
        setHandler();
        setMonthBorder();
    }

    private void buildWeekendBlock() {
        container.add(saturday);
        container.add(sunday);
        saturday.add(date);
        sunday.add(date2);
        date.setText(saturdayDate.getDate() + "");
        date2.setText(sundayDate.getDate() + "");


    }

    private void setStyle() {
        saturday.addStyleName("calendar-holiday-block");

        sunday.addStyleName("calendar-holiday-block");
        date.addStyleName("calendar-block-date");
        date2.addStyleName("calendar-block-date");

    }

    private void setMonthBorder(){
        FlowPanel flowPanel = new FlowPanel();

        if (saturdayDate.getDate() == 1) {
            saturday.addStyleName("first-day-month");
            flowPanel.addStyleName("first");
            flowPanel.add(new HTML(month.get(saturdayDate.getMonth()) + ""));
            container.add(saturday);
            sunday.addStyleName("second-weekend");
            container.add(sunday);
        } else if (saturdayDate.getDate() < 8 && saturdayDate.getDate() -6 != 1) {
            saturday.addStyleName("days-of-first-week");
        } else if (sundayDate.getDate() == 1) {

            flowPanel.addStyleName("first2");
            flowPanel.add(new HTML(month.get(sundayDate.getMonth())+""));
            container.add(sunday);
            sunday.addStyleName("first-day-month");
        } else if (saturdayDate.getDate() < 8 && saturdayDate.getDate() > 8) {
            if (saturdayDate.getDate() != 1) {
                sunday.addStyleName("days-of-first-week");
            }

        }
        container.add(flowPanel);

    }

    private void setHandler() {
        saturday.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent();
                saturday.addStyleName("calendar-focus-day-block");
            }
        });

        sunday.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent();
                sunday.addStyleName("calendar-focus-day-block");

            }
        });
    }

    @Override
    public void resetSelectionStyle() {
        saturday.removeStyleName("calendar-focus-day-block");
        sunday.removeStyleName("calendar-focus-day-block");


    }

}
