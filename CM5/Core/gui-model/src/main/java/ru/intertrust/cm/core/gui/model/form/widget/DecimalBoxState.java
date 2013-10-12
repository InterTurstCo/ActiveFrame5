package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 11:40
 */
public class DecimalBoxState extends WidgetState {
    private BigDecimal value;

    public DecimalBoxState() {
    }

    public DecimalBoxState(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Value toValue() {
        return new DecimalValue(value);
    }
}
