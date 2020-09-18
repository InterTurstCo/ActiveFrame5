package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Value;

public class InSimpleDataSearchFilter extends AbsSimpleDataSearchFilter {

    public InSimpleDataSearchFilter() {
    }

    public InSimpleDataSearchFilter(String fieldName, Value<?> fieldValue) {
        super(fieldName, fieldValue);
    }
}
