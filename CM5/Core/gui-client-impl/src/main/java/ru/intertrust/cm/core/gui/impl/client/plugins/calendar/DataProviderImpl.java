package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;

import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarData;

/**
 * FIXME temporary class
 *
 * @author Sergey.Okolot
 *         Created on 13.10.2014 15:39.
 */
public class DataProviderImpl extends AbstractDataProvider<CalendarData> {

    @Override
    protected void onRangeChanged(HasData<CalendarData> display) {
        final int fromIndex = display.getVisibleRange().getStart();
        final int toIndex = display.getVisibleRange().getLength();
        final List<CalendarData> data = new ArrayList<>(); // getDataList();
        if (toIndex < data.size()) {
            this.updateRowData(fromIndex, data.subList(fromIndex, toIndex));
        } else if (fromIndex < data.size()) {
            this.updateRowData(fromIndex, data.subList(fromIndex, data.size()));
        }
    }
}
