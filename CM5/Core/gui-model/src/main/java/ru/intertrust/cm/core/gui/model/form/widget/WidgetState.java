package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;

/**
 * Состояние виджета, определяющее его внешний вид.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetState implements Dto {
    protected boolean editable;

    public abstract Value toValue();

    // todo: not sure if it's needed. toIds() may be for those widgets which work with multi-objects?
    public ArrayList<Value> toValues() {
        ArrayList<Value> result = new ArrayList<Value>(1);
        result.add(toValue());
        return result;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean equals(Object obj) { // todo: implement in all widgets!
        return super.equals(obj);
    }
}
