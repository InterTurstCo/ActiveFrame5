package ru.intertrust.cm.core.gui.impl.client.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.plugins.calendar.CalendarPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;
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

    private Date selectedDate;
    private Map<Date, List<CalendarItemData>> values = new HashMap<>();
    private Date valuesFrom;
    private Date valuesTo;
    private boolean isReady = true;

    public CalendarTableModel(final CalendarPlugin owner) {
        this.owner = owner;
        final CalendarPluginData initialData = owner.getInitialData();
        valuesFrom = initialData.getFromDate();
        valuesTo = initialData.getToDate();
        values = initialData.getValues();
        selectedDate = initialData.getSelectedDate();
        CalendarUtil.resetTime(valuesFrom);
        CalendarUtil.resetTime(valuesTo);
        CalendarUtil.resetTime(selectedDate);
    }

    public void fillByDateValues(final Date date, final CalendarTableModelCallback callback) {
        if (valuesFrom == null) {

        }
        if (date.before(valuesFrom)) {

        }


        if (values == null) {
            valuesFrom = CalendarUtil.copyDate(selectedDate);
            CalendarUtil.addDaysToDate(valuesFrom, -30);
            valuesTo = CalendarUtil.copyDate(selectedDate);
            CalendarUtil.addDaysToDate(valuesTo, 30);
            requestRows(valuesFrom, valuesTo, new RequestCallback() {
                @Override
                public void valuesReady() {
                    callback.fillValues(values.get(date));
                }
            });
        } else {
            callback.fillValues(values.get(date));
        }
    }

    private void requestRows(final Date fromDate, final Date toDate, final RequestCallback requestCallback) {
        isReady = false;
        final AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {

            @Override
            public void onFailure(Throwable caught) {
                Application.getInstance().hideLoadingIndicator();
                isReady = true;
            }

            @Override
            public void onSuccess(Dto result) {
                Application.getInstance().hideLoadingIndicator();
                final CalendarRowsResponse data = (CalendarRowsResponse) result;
                if (values == null || values.size() > MAX_VALUES_SIZE) {
                    values = data.getValues();
                } else {
                    values.putAll(data.getValues());
                }
                isReady = true;
                if (requestCallback != null) {
                    requestCallback.valuesReady();
                }
            }
        };
        Application.getInstance().showLoadingIndicator();
        final CalendarConfig calendarConfig = (CalendarConfig) owner.getConfig();
        final CalendarRowsRequest request = new CalendarRowsRequest(calendarConfig, fromDate, toDate);
        final Command command = new Command("requestRows", CalendarConfig.COMPONENT_NAME, request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback);
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(final Date selectedDate) {
        this.selectedDate = CalendarUtil.copyDate(selectedDate);
        CalendarUtil.resetTime(this.selectedDate);
//        if (valuesFrom == null) {
//            valuesFrom = CalendarUtil.copyDate(this.selectedDate);
//            CalendarUtil.addDaysToDate(valuesFrom, -30);
//            valuesTo = CalendarUtil.copyDate(this.selectedDate);
//            CalendarUtil.addDaysToDate(valuesTo, 30);
//            requestRows(valuesFrom, valuesTo, null);
//        }
    }

    private interface RequestCallback {
        void valuesReady();
    }
}
