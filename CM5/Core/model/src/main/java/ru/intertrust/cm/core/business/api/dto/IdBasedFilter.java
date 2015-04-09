package ru.intertrust.cm.core.business.api.dto;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vmatsukevich
 * Date: 12/2/13
 * Time: 10:45 AM
 */
public class IdBasedFilter extends Filter {

    public IdBasedFilter() {
    }

    public IdBasedFilter(Filter filter) {
        super(filter);
    }
    
    public IdBasedFilter(List<ReferenceValue> ids) {
        if (ids == null) {
            return;
        }

        for (int i = 0; i < ids.size(); i ++) {
            addCriterion(i, ids.get(i));
        }
    }

    public IdBasedFilter(ReferenceValue... ids) {
        for (int i = 0; i < ids.length; i ++) {
            addCriterion(i, ids[i]);
        }
    }

    @Override
    public void addCriterion(int index, Value value) {
        validateValueType(value);
        super.addCriterion(index, value);
    }

    @Override
    public ReferenceValue getCriterion(int index) {
        return (ReferenceValue) super.getCriterion(index);
    }

    @Override
    public void addMultiCriterion(int index, List<Value> value) {
        throw new UnsupportedOperationException();
    }

    private void validateValueType(Value value) {
        if (value != null && !(value instanceof ReferenceValue)) {
            throw new IllegalArgumentException("Illegal argument type " + value.getClass() + ". " +
                    ReferenceValue.class + " is expected.");
        }
    }
}
