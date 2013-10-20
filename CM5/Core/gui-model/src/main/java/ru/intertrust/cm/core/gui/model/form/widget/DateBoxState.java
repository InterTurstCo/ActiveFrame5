package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxState extends WidgetState {
    private Date value;

    public DateBoxState() {
    }

    public DateBoxState(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    @Override
    public Value toValue() {
        return new TimestampValue(value);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
