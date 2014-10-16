package ru.intertrust.cm.core.gui.impl.client.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Sergey.Okolot
 *         Created on 16.10.2014 13:35.
 */
public class CalendarTableModel {

    private Map<Date, List<? extends Dto>> values;
    private Date fromDate;
    private Date toDate;

    public List<? extends Dto> getValues(Date date) {
        return values.get(date);
    }

    public void reloadValues(final Date fromDate, final Date toDate) {
        values.clear();
        this.fromDate = fromDate;
        this.toDate = toDate;
        requestRows(fromDate, toDate);
    }

    public void setRange(Date from, Date to) {
        // fixme check NPE and other cases
        if (!from.before(to)) {
            throw new IllegalArgumentException();
        }
        Date startRange = null;
        Date endRange = null;
        if (from.before(fromDate)) {
            startRange = from;
            endRange = fromDate;
        } else if (to.after(toDate)) {
            startRange = toDate;
            endRange = to;
        }
        requestRows(startRange, endRange);
    }

    private void requestRows(final Date fromDate, final Date toDate) {
        final AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {

            @Override
            public void onFailure(Throwable caught) {
                Application.getInstance().hideLoadingIndicator();
            }

            @Override
            public void onSuccess(Dto result) {
                Application.getInstance().hideLoadingIndicator();
                final CalendarRowsResponse data = (CalendarRowsResponse) result;
                values.putAll(data.getValues());
            }
        };
        Application.getInstance().showLoadingIndicator();
        final CalendarRowsRequest request = new CalendarRowsRequest(fromDate, toDate);
        final Command command = new Command("requestRows", CalendarPluginData.COMPONENT_NAME, request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback);
    }
}
