package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 *
 *  Состояние виджета, позволяющего выбирать одно значение из нескольких возможных (combo-box, radio-button group).
 */
public abstract class SingleSelectionWidgetState extends ValueListWidgetState {
    private Id selectedId;

    public Id getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Id selectedId) {
        this.selectedId = selectedId;
    }

    @Override
    public boolean isSingleChoice() {
        return true;
    }

    @Override
    public ArrayList<Id> getIds() {
        ArrayList<Id> list = new ArrayList<Id>();
        list.add(selectedId);
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
