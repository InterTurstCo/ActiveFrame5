package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.CalendarSelectDayEvent;

import java.util.ArrayList;

/**
 * Created by lvov on 04.04.14.
 */
public abstract class CalendarDayView {

    public static final int CALENDAR_COLUMN_COUNT = 6;
    protected AbsolutePanel container = new AbsolutePanel();
    protected EventBus eventBus;
    public static final ArrayList<String> month;
    private Plugin plugin;

    static {
        month = new ArrayList<String>();
        month.add("Январь");
        month.add("Февраль");
        month.add("Март");
        month.add("Апрель");
        month.add("Май");
        month.add("Июнь");
        month.add("Июль");
        month.add("Август");
        month.add("Сентябрь");
        month.add("Октябрь");
        month.add("Ноябрь");
        month.add("Декабрь");
    }

    protected CalendarDayView(EventBus eventBus, Plugin plugin) {
        this.eventBus = eventBus;
        this.plugin = plugin;
        container.addStyleName("calendar-day-container");
        //container.addStyleName("some");

        sizeHandler();

    }

    public AbsolutePanel getContainer() {
        return container;
    }

    public void setContainer(AbsolutePanel container) {
        this.container = container;
    }

    public abstract void resetSelectionStyle();

    protected void fireEvent() {
        eventBus.fireEvent(new CalendarSelectDayEvent(this));

    }

    private void sizeHandler(){

        container.setWidth(minCalendarDayWidth((plugin.getOwner().getVisibleWidth()/CALENDAR_COLUMN_COUNT))+"px");
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                container.setWidth(minCalendarDayWidth((plugin.getOwner().getVisibleWidth()/CALENDAR_COLUMN_COUNT))+"px");
            }
        });

    }

    private int minCalendarDayWidth(int width){
//        if (width < 200){
//            width = 200;
//        }
        return width+2;
    }


}
