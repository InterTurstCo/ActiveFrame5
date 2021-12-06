package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Value;

public class LikeSimpleDataSearchFilter extends AbsSimpleDataSearchFilter {

    public LikeSimpleDataSearchFilter() {
    }

    public LikeSimpleDataSearchFilter(String fieldName, Value<?> fieldValue) {
        super(fieldName, fieldValue);
    }
}
