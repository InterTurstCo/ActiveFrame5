package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */

public class ComboBoxState extends ListWidgetState {

    private Id selectedId;
    private List<DomainObject> originalObjects;

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

    public List<DomainObject> getOriginalObjects() {
        return originalObjects;
    }

    public void setOriginalObjects(List<DomainObject> originalObjects) {
        this.originalObjects = originalObjects;
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
}
