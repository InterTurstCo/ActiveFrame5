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
public class SingleSelectionWidgetState extends ValueEditingWidgetState {
    private Id id;
    private LinkedHashMap<Id, String> listValues; //declared as LinkedHashMap (rather then Map) intentionally, to emphasize that items order is important

    public SingleSelectionWidgetState() {
    }

    public SingleSelectionWidgetState(Id id) {
        this.id = id;
    }

    public SingleSelectionWidgetState(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
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
