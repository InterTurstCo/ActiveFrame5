package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;

/**
 * @author Sergey.Okolot
 *         Created on 30.10.2014 12:45.
 */
public abstract class CalendarPanel extends FlowPanel implements RequiresResize {
    protected static final String SELECTED_DATE_STYLE = "calendar-focus-day-block";

    protected final List<HandlerRegistration> handlers = new ArrayList<>();
    protected final CalendarConfig calendarConfig;
    protected final EventBus localEventBus;
    protected final CalendarTableModel tableModel;
    protected Date currentDate = new Date();
    private AbstractDateItem selectedItem;

    public CalendarPanel(final EventBus localEventBus, final CalendarTableModel tableModel, final CalendarConfig config) {
        this.tableModel = tableModel;
        this.localEventBus = localEventBus;
        this.calendarConfig = config;
        CalendarUtil.resetTime(currentDate);
        setStyleName("calendar-scroll-panel");
    }

    @Override
    protected void onDetach() {
        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }
        super.onDetach();
    }

    protected abstract class AbstractWeekendItem extends FlowPanel {

        protected AbstractWeekendItem(final Date startDate, final int width, int height) {
            final Style style = getElement().getStyle();
            style.setFloat(Style.Float.LEFT);
            style.setWidth(width - 6, Style.Unit.PX);
            style.setHeight(height - 1, Style.Unit.PX);
            final int childrenHeight = height / 2;
            AbstractDateItem item = createDateItem(startDate, width, childrenHeight);
            add(item);
            CalendarUtil.addDaysToDate(startDate, 1);
            item = createDateItem(startDate, width, childrenHeight);
            add(item);
        }

        protected abstract AbstractDateItem createDateItem(Date date, int width, int height);
    }

    protected abstract class AbstractDateItem extends FlowPanel implements ClickHandler {

        protected final Date date;
        private FlowPanel itemWrapper = new FlowPanel();

        protected AbstractDateItem(final Date date, int width, int height) {
            this.date = CalendarUtil.copyDate(date);
            width -= 1;
            height -= 1;
            itemWrapper.setStyleName("calendar-day-wrapper");
            setStyleName("calendar-day-block");
            if (CalendarUtil.isSameDate(date, tableModel.getSelectedDate())) {
                selectedItem = this;
                itemWrapper.addStyleName(SELECTED_DATE_STYLE);
            }
            addStyles();
            getElement().getStyle().setWidth(width, Style.Unit.PX);
            getElement().getStyle().setHeight(height, Style.Unit.PX);
            itemWrapper.add(getItemLabel(date));
            itemWrapper.add(getTasksPanel());
            add(itemWrapper);
            sinkEvents(Event.ONCLICK);
            addHandler(this, ClickEvent.getType());
        }

        @Override
        public void onClick(ClickEvent event) {
            if (selectedItem != null) {
                selectedItem.itemWrapper.removeStyleName(SELECTED_DATE_STYLE);
            }
            selectedItem = this;
            tableModel.setSelectedDate(date);
            itemWrapper.addStyleName(SELECTED_DATE_STYLE);
        }

        protected abstract Label getItemLabel(Date date);

        protected abstract Widget getTasksPanel();

        protected abstract void addStyles();
    }
}
