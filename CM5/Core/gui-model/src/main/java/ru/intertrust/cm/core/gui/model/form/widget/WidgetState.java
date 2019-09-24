package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.RulesTypeConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Состояние виджета, определяющее его внешний вид.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetState implements Dto {
    protected boolean editable;
    protected boolean forceReadOnly;
    protected boolean translateId;
    private HashMap<String, Object> widgetProperties = new HashMap<String, Object>(); // declared as HashMap rather than Map because Map is not serializable
    private List<Constraint> constraints = new ArrayList<Constraint>();
    private Set<String> subscription = new HashSet<>();
    private String widgetId;
    private RulesTypeConfig rules;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isForceReadOnly() {
        return forceReadOnly;
    }

    public void setForceReadOnly(boolean forceReadOnly) {
        this.forceReadOnly = forceReadOnly;
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

    public boolean isTranslateId() {
        return translateId;
    }

    public void setTranslateId(boolean translateId) {
        this.translateId = translateId;
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

    public Set<String> getSubscription() {
        return subscription;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public RulesTypeConfig getRules() {
        return rules;
    }

    public void setRules(RulesTypeConfig rules) {
        this.rules = rules;
    }
}
