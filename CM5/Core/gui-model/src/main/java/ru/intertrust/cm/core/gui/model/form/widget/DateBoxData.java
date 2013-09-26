package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxData extends WidgetData {
    private Date value;

    public DateBoxData() {
    }

    public DateBoxData(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    @Override
    public String getComponentName() {
        return "date-box";
    }

    @Override
    public Value toValue() {
        return new TimestampValue(value);
    }
}
