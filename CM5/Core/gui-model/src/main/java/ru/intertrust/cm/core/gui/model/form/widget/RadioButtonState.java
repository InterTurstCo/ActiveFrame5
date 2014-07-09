package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Lesia Puhova
 *         Date: 16.01.2014
 *         Time: 11:42:27
 */

public class RadioButtonState extends ListWidgetState {

    public enum Layout {
        VERTICAL,
        HORIZONTAL
    }

    private Id selectedId;
    private Layout layout;

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
        if (selectedId != null) {
            list.add(selectedId);
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

}
