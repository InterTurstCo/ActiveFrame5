package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 *
 *  Состояние виджета, позволяющего выбирать одно значение из нескольких возможных (combo-box, radio-button group).
 */
public abstract class SingleSelectionWidgetState extends ValueEditingWidgetState {
    private Id selectedId;
    private LinkedHashMap<Id, String> listValues; //declared as LinkedHashMap (rather then Map) intentionally, to emphasize that items order is important

    public SingleSelectionWidgetState() {
    }

    public SingleSelectionWidgetState(Id selectedId) {
        this.selectedId = selectedId;
    }

    public SingleSelectionWidgetState(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public Id getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Id selectedId) {
        this.selectedId = selectedId;
    }

    public Map<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
