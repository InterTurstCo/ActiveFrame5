package ru.intertrust.cm.core.gui.model.form.widget;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 11:40
 */
public class DecimalBoxState extends SingleObjectWidgetState {
    private BigDecimal number;

    public DecimalBoxState() {
    }

    public DecimalBoxState(BigDecimal number) {
        this.number = number;
    }

    public BigDecimal getNumber() {
        return number;
    }

    public void setNumber(BigDecimal number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
