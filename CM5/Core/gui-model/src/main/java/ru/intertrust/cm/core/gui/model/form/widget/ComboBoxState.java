package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class ComboBoxState extends WidgetState {
    private Id id;
    private LinkedHashMap<Id, String> listValues;

    public ComboBoxState() {
    }

    public ComboBoxState(Id id) {
        this.id = id;
    }

    public ComboBoxState(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    @Override
    public Value toValue() {
        return new ReferenceValue(id);
    }
}
