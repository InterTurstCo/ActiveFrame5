package ru.intertrust.cm.core.gui.model.form.widget;

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
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
