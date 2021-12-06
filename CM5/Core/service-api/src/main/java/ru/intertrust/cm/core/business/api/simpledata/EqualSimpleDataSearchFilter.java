package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Value;

public class EqualSimpleDataSearchFilter extends AbsSimpleDataSearchFilter {

    public EqualSimpleDataSearchFilter() {
    }

    public EqualSimpleDataSearchFilter(String fieldName, Value<?> fieldValue) {
        super(fieldName, fieldValue);
    }
}
