package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Состояние виджета, определяющее его внешний вид.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetState implements Dto {
    protected boolean editable;
    private HashMap<String, Object> widgetProperties = new HashMap<String, Object>(); // declared as HashMap rather than Map because Map is not serializable
    private List<Constraint> constraints = new ArrayList<Constraint>();

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean mayContainNestedFormStates() {
        return false;
    }

    public LinkedHashMap<String, FormState> getEditedNestedFormStates() {
        return null;
    }

    public HashMap<String, Object> getWidgetProperties() {
        return widgetProperties;
    }

    public void setWidgetProperties(HashMap<String, Object> widgetProperties) {
        this.widgetProperties = widgetProperties;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }

    @Override
    public boolean equals(Object obj) { // todo: implement in all widgets!
        return super.equals(obj);
    }
}
