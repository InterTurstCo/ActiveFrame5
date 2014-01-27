package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */

public class ComboBoxState extends ListWidgetState {

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
