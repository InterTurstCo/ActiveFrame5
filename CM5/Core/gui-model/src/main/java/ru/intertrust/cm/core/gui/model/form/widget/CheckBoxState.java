package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
public class CheckBoxState extends WidgetState {
    private Boolean isSelected;
    private String text;

    public CheckBoxState() {
    }

    public CheckBoxState(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public Boolean isSelected() {
        return isSelected;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
