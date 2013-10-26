package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxState extends ValueEditingWidgetState {
    private Date date;

    public DateBoxState() {
    }

    public DateBoxState(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Value getValue() {
        return new TimestampValue(date);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
