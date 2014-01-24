package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
public class CheckBoxState extends SingleObjectWidgetState {
    private Boolean isSelected;

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
}
