package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;

import java.util.List;

public class InSimpleDataSearchFilter extends AbsSimpleDataSearchFilter {

    public InSimpleDataSearchFilter() {
    }

    public InSimpleDataSearchFilter(String fieldName, Value<?> fieldValue) {
        super(fieldName, fieldValue);
    }

    public InSimpleDataSearchFilter(String fieldName, List<Value<?>> fieldValue) {
        super(fieldName, new ListValue(fieldValue != null ? fieldValue.toArray(new Value<?>[fieldValue.size()]) : null));
    }

}
