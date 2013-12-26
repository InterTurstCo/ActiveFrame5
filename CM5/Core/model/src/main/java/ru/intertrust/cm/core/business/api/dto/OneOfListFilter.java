package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OneOfListFilter extends SearchFilterBase {

    private ArrayList<ReferenceValue> values = new ArrayList<>();

    public OneOfListFilter() {
    }

    public OneOfListFilter(String fieldName) {
        super(fieldName);
    }

    public OneOfListFilter(String fieldName, Collection<? extends ReferenceValue> values) {
        super(fieldName);
        
    }

    public List<ReferenceValue> getValues() {
        return values;
    }

    public void addValue(ReferenceValue value) {
        values.add(value);
    }

    public void addValue(Id id) {
        values.add(new ReferenceValue(id));
    }

    public void addValues(Collection<? extends ReferenceValue> values) {
        this.values.addAll(values);
    }

    public void removeValue(ReferenceValue value) {
        values.remove(value);
    }

    public void removeValue(Id id) {
        values.remove(new ReferenceValue(id));
    }

    public void clearValues() {
        values = new ArrayList<>();
    }
}
