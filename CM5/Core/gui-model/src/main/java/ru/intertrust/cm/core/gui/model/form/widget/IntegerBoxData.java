package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 15:09
 */
public class IntegerBoxData extends WidgetData {
    private Long value;

    public Long getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value == null ? null : (long) value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
