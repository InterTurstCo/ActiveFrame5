package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Value;

public class BeforeSimpleDataSearchFilter extends AbsSimpleDataSearchFilter {

    private boolean exclusive;

    public BeforeSimpleDataSearchFilter() {
    }

    public BeforeSimpleDataSearchFilter(String fieldName, Value<?> fieldValue) {
        super(fieldName, fieldValue);
    }

    public BeforeSimpleDataSearchFilter(String fieldName, Value<?> fieldValue, boolean exclusive) {
        super(fieldName, fieldValue);
        this.exclusive = exclusive;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
}
