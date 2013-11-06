package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
public class CheckBoxState extends ValueEditingWidgetState {
    private Boolean isSelected;

    public CheckBoxState() {
    }

    public CheckBoxState(Boolean isSelected) {
        this.isSelected = isSelected;
    }
    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    @Override
    public Value getValue() {
        return new BooleanValue(isSelected);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
