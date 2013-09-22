package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 15:09
 */
public class IntegerBoxData extends WidgetData {
    private Long value;

    public IntegerBoxData() {
    }

    public IntegerBoxData(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value == null ? null : (long) value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public String getComponentName() {
        return "integer-box";
    }

    @Override
    public Value toValue() {
        return new LongValue(value);
    }
}
