package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lvov on 04.04.14.
 */
public class CalendarWorkingDayView extends CalendarDayView {

    private FocusPanel workingDay = new FocusPanel();
    private Label date = new Label();
    private Date workingDayDate;



    protected CalendarWorkingDayView(Date workingDayDate, EventBus eventBus, Plugin plugin) {
        super(eventBus, plugin);
        this.workingDayDate = workingDayDate;

        buildWeekendBlock();
        setStyle();
        setHandler();
        setMonthBorder();
    }

    @Override
    public void resetSelectionStyle(){
        workingDay.removeStyleName("calendar-focus-day-block");

    }

    private void buildWeekendBlock(){
        container.add(workingDay);
        workingDay.add(date);
        date.setText(workingDayDate.getDate()+"");


    }

    private void setStyle(){
        workingDay.addStyleName("calendar-work-day-block");

     //   date.removeStyleName("gwt-Label");
        date.addStyleName("calendar-block-date");
    }

    private void setMonthBorder(){
        if (workingDayDate.getDate() == 1){
            workingDay.addStyleName("first-day-month");
            FlowPanel flowPanel = new FlowPanel();
            flowPanel.addStyleName("first");
            flowPanel.add(new HTML(month.get(workingDayDate.getMonth())+""));
            container.add(flowPanel);

        }  else if (workingDayDate.getDate() <8){
            workingDay.addStyleName("days-of-first-week");
        }

        if (workingDayDate.getDate() == new Date().getDate() &&  workingDayDate.getMonth() == new Date().getMonth()){
            workingDay.addStyleName("today");

        }


    }


    private void setHandler(){
        workingDay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent();
                workingDay.addStyleName("calendar-focus-day-block");
//                Date realDate = new Date();
//                Date date1 = new Date();
//                date1.
//
//                Window.alert(realDate+"");
            }
        });
    }

}
