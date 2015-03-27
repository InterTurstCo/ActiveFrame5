package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 15:09
 */
public class IntegerBoxState extends WidgetState {
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
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
