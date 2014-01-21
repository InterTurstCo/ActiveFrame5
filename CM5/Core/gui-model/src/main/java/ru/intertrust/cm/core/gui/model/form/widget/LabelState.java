package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 13:41
 */
public class LabelState extends WidgetState {
    private String label;
    private boolean relatedToRequiredField;

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

    public boolean isRelatedToRequiredField() {
        return relatedToRequiredField;
    }

    public void setRelatedToRequiredField(boolean relatedToRequiredField) {
        this.relatedToRequiredField = relatedToRequiredField;
    }

    @Override
    // modified to return true always, so to make isEditable() method of LabelWidget
    // to indicate whether containing panel is editable or not.
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
