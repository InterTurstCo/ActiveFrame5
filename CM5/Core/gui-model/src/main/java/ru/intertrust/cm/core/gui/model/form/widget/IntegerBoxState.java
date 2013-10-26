package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 15:09
 */
public class IntegerBoxState extends ValueEditingWidgetState {
    private Long number;

    public IntegerBoxState() {
    }

    public IntegerBoxState(Long number) {
        this.number = number;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number == null ? null : (long) number;
    }

    public void setValue(Long value) {
        this.number = value;
    }

    @Override
    public Value getValue() {
        return new LongValue(number);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
