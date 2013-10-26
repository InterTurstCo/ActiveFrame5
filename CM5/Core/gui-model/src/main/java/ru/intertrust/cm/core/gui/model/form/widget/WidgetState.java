package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Состояние виджета, определяющее его внешний вид.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetState implements Dto {
    protected boolean editable;

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
