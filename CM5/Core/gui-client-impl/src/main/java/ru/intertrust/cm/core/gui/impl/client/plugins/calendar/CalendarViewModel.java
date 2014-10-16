package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Sergey.Okolot
 *         Created on 13.10.2014 15:00.
 */
public class CalendarViewModel<T> {

    private final AbstractDataProvider<T> dataProvider;
    private final SingleSelectionModel<Date> selectionModel = new SingleSelectionModel<>();
    private Cell<T> cell;

    public CalendarViewModel(final AbstractDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
    }

    public SingleSelectionModel<Date> getSelectionModel() {
        return selectionModel;
    }
}
