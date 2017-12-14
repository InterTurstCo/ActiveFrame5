package ru.intertrust.cm.core.gui.impl.client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarSelectDateListener;
import ru.intertrust.cm.core.gui.impl.client.plugins.calendar.CalendarPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Sergey.Okolot
 *         Created on 16.10.2014 13:35.
 */
public class CalendarTableModel {

    private static final int MAX_VALUES_SIZE = 180;

    private final CalendarPlugin owner;

    private List<CalendarSelectDateListener> selectListeners = new ArrayList<>();
    private final Map<Date, List<CalendarItemsData>> values = new HashMap<>();
    private final List<QueueItem> queueItems = new ArrayList<>();
    private Date selectedDate;
    private Date valuesFrom;
    private Date valuesTo;
    private boolean ready;

    public CalendarTableModel(final CalendarPlugin owner) {
        this.owner = owner;
        final CalendarPluginData initialData = owner.getInitialData();
        values.putAll(initialData.getValues());
        selectedDate = initialData.getSelectedDate();
        CalendarUtil.resetTime(selectedDate);
        setRange(initialData.getFromDate(), initialData.getToDate());
        ready = true;
    }

    public void fillByDateValues(final Date date, final CalendarTableModelCallback callback) {
        if (date.before(valuesTo) && date.after(valuesFrom)) {
            callback.fillValues(values.get(date));
        } else {
            final Date from;
            final Date to;
            if (date.before(valuesFrom)) {
                to = CalendarUtil.copyDate(valuesFrom);
                from = CalendarUtil.copyDate(date);
                CalendarUtil.addMonthsToDate(from, -1);
            } else {
                from = CalendarUtil.copyDate(valuesTo);
                to = CalendarUtil.copyDate(date);
                CalendarUtil.addMonthsToDate(to, 1);
            }
            queueItems.add(new QueueItem(date, callback));
            requestRows(from, to);
        }
    }

    private void requestRows(final Date fromDate, final Date toDate) {
        if (ready) {
            ready = false;
            final AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {

                @Override
                public void onFailure(Throwable caught) {
                    Application.getInstance().unlockScreen();
                    ready = true;
                }

                @Override
                public void onSuccess(Dto result) {
                    Application.getInstance().unlockScreen();
                    final CalendarRowsResponse data = (CalendarRowsResponse) result;
                    if (values.size() > MAX_VALUES_SIZE) {
                        values.clear();
                        values.putAll(data.getValues());
                        setRange(data.getFrom(), data.getTo());
                    } else {
                        values.putAll(data.getValues());
                        updateRange(data.getFrom(), data.getTo());
                    }
                    for (Iterator<QueueItem> it = queueItems.iterator(); it.hasNext(); ) {
                        final QueueItem item = it.next();
                        item.getCallback().fillValues(values.get(item.getDate()));
                        it.remove();
                    }
                    ready = true;
                }
            };
            Application.getInstance().lockScreen();
            final CalendarConfig calendarConfig = (CalendarConfig) owner.getConfig();
            final CalendarRowsRequest request = new CalendarRowsRequest(calendarConfig, fromDate, toDate);
            final Command command = new Command("requestRows", CalendarConfig.COMPONENT_NAME, request);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, callback);
        }
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(final Date date) {
        this.selectedDate = CalendarUtil.copyDate(date);
        CalendarUtil.resetTime(selectedDate);
        for (CalendarSelectDateListener listener : selectListeners) {
            listener.onSelect(date);
        }
    }

    public void addSelectListener(final CalendarSelectDateListener listener) {
        if (!selectListeners.contains(listener)) {
            selectListeners.add(listener);
        }
    }

    public void removeListener(final CalendarSelectDateListener listener) {
        selectListeners.remove(listener);
    }

    private void updateRange(final Date from, final Date to) {
        CalendarUtil.resetTime(from);
        CalendarUtil.resetTime(to);
        if (from.before(this.valuesFrom)) {
            valuesFrom = CalendarUtil.copyDate(from);
        }
        if (to.after(this.valuesTo)) {
            valuesTo = CalendarUtil.copyDate(to);
        }
    }

    private void setRange(final Date from, final Date to) {
        CalendarUtil.resetTime(from);
        CalendarUtil.resetTime(to);
        valuesFrom = CalendarUtil.copyDate(from);
        valuesTo = CalendarUtil.copyDate(to);
    }

    private static class QueueItem {
        private final Date date;
        private final CalendarTableModelCallback callback;

        private QueueItem(Date date, CalendarTableModelCallback callback) {
            this.date = date;
            this.callback = callback;
        }

        public Date getDate() {
            return date;
        }

        public CalendarTableModelCallback getCallback() {
            return callback;
        }
    }
}
