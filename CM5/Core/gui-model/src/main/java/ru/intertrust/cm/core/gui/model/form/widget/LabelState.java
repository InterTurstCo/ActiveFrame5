package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 13:41
 */
public class LabelState extends WidgetState {
    private String label;

    public LabelState() {
    }

    public LabelState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Value toValue() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
